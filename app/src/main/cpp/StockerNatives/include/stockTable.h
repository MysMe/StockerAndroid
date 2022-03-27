#pragma once
#include <iostream>
#include <map>
#include <vector>
#include <algorithm>
#include <array>
#include <fstream>
#include <string>
#include <filesystem>
#include <sstream>
#include <type_traits>

#include "lookupTable.h"

struct stockElement
{
    std::string name;
    std::string size;

    bool operator<(const stockElement& other) const
    {
        int c1 = std::strncmp(name.data(), other.name.data(), std::min(name.size(), other.name.size()));
        if (c1 < 0)
            return true;
        if (c1 == 0)
            return size < other.size;
        return false;
    }

    std::vector<std::pair<size_t, float>> counts;

    void alter(size_t location, float delta)
    {
        for (auto& [l, c] : counts)
        {
            if (l == location)
            {
                c += delta;
                return;
            }
        }
        counts.emplace_back(location, delta);
    }

    float sum() const
    {
        float ret = 0;
        for (const auto& [l, c] : counts)
            ret += c;
        return ret;
    }

    float getCount(size_t location) const
    {
        for (auto& [l, c] : counts)
        {
            if (l == location)
                return c;
        }
        return 0;
    }
};



class stockTable
{

private:
    class inputStockSource
    {
    public:
        virtual inputStockSource& getLine(std::string& out) = 0;
        virtual operator bool() const = 0;
    };

    class inputStream final : public inputStockSource
    {
        std::reference_wrapper<std::istream> stream;
    public:

        inputStream(std::istream& source) : stream(source) {}

        inputStockSource& getLine(std::string& out) final
        {
            std::getline(stream.get(), out);
            return *this;
        }
        operator bool() const final
        {
            return static_cast<bool>(stream.get());
        }
    };

    class inputFILE final : public inputStockSource
    {
        FILE* ptr;
    public:

        inputFILE() = delete;
        inputFILE(FILE* src) : ptr(src) {}

        inputStockSource& getLine(std::string& out) final
        {
            out.clear();
            if (std::ferror(ptr) || std::feof(ptr))
                return *this;
            int c = 0;
            while ((c = std::fgetc(ptr)) != EOF && c != '\n')
                out.push_back(c);
            return *this;
        }

        operator bool() const final
        {
            return std::ferror(ptr) == 0 && std::feof(ptr) == 0;
        }
    };

    class outputStockSource
    {
    public:
        virtual outputStockSource& operator<<(const char v) = 0;
        virtual outputStockSource& operator<<(const std::string& v) = 0;
        template <class T>
        std::enable_if_t<std::is_arithmetic_v<T>, outputStockSource&> operator<<(T v)
        {
            *this << std::to_string(v);
            return *this;
        }
        virtual operator bool() const = 0;
    };

    class outputStream final : public outputStockSource
    {
        std::reference_wrapper<std::ostream> stream;
    public:

        outputStream(std::ostream& source) : stream(source) {}

        outputStockSource& operator<<(const char v) final
        {
            stream.get() << v;
            return *this;
        }

        outputStockSource& operator<<(const std::string& v) final
        {
            stream.get() << v;
            return *this;
        }

        operator bool() const final
        {
            return stream.get().operator bool();
        }
    };

    class outputFILE final : public outputStockSource
    {
        FILE* file = nullptr;
    public:

        outputFILE(FILE* source) : file(source) {}

        outputStockSource& operator<<(const char v) final
        {
            std::fputc(v, file);
            return *this;
        }

        outputStockSource& operator<<(const std::string& v) final
        {
            std::fputs(v.c_str(), file);
            return *this;
        }

        operator bool() const final
        {
            return std::ferror(file) == 0 && std::feof(file) == 0;
        }
    };

    static void stringToUpper(std::string& str)
    {
        std::transform(str.begin(), str.end(), str.begin(), [](unsigned char c) -> unsigned char { return static_cast<unsigned char>(std::toupper(c)); });
    }

    std::vector<stockElement> elements;
    using elementIterator = decltype(elements)::const_iterator;

    std::vector<std::string> locationIndices;
    size_t location = 0;

    void reset()
    {
        elements.clear();
        locationIndices.clear();
        location = 0;
    }

    //Cleans and organises stock entries (removing invalid characters, etc)
    void tidy()
    {
        for (auto& i : elements)
        {
            //Standardise layout
            stringToUpper(i.name);

            //Remove superfluous quotation marks
            if (i.name.front() == '\"' && i.name.back() == '\"')
            {
                i.name.erase(i.name.begin());
                i.name.pop_back();
            }
        }
    }

public:
    enum class fileError
    {
        none = 0,
        unable_to_open,
        bad_BOM,
        bad_header,
        unable_to_parse_count
    };

    class stockTableFullSearch
    {
        struct comp
        {
            comp(stockTable::elementIterator beg) : begin(beg) {}

            stockTable::elementIterator begin;
            bool operator()(const stockTable::elementIterator& l, const stockTable::elementIterator& r)
            {
                if (l->name != r->name)
                    return l->name < r->name;
                return std::distance(begin, l) < std::distance(begin, r);
            }
        };

        std::vector<stockTable::elementIterator> iterators;
    public:

        using const_iterator = decltype(iterators)::const_iterator;

        stockTableFullSearch(const stockTable& table)
        {
            iterators.reserve(table.elements.size());
            for (auto it = table.elements.cbegin(); it != table.elements.cend(); ++it)
                iterators.push_back(it);
            std::sort(iterators.begin(), iterators.end(), comp(table.elements.begin()));
        }

        auto search(const std::string& keyword) const
        {
            struct cmp
            {
                bool operator()(const stockTable::elementIterator& elem, const std::string& str) const
                {
                    return elem->name < str;
                }
                bool operator()(const std::string& str, const stockTable::elementIterator& elem) const
                {
                    return str < elem->name;
                }
            };
            return std::equal_range(iterators.cbegin(), iterators.cend(), keyword, cmp());
        }
    };
private:

    static void clearQuotes(std::string& in)
    {
        if (in.size() < 2 || in.front() != '\"' || in.back() != '\"')
            return;
        in.pop_back();
        in.erase(in.begin());
    }

    static auto getField(std::string line, size_t off) -> std::pair<std::string, size_t>
    {
        bool isQuoted = false;
        for (size_t i = off; i < line.size(); i++)
        {
            if (line[i] == '\"')
                isQuoted = !isQuoted;
            if (line[i] == ',' && !isQuoted)
            {
                std::string ret = line.substr(off, i - off);
                clearQuotes(ret);
                return { ret, i == line.size() - 1 ? std::string::npos : i + 1 };
            }
        }
        std::string ret = line.substr(off, line.size() - off);
        clearQuotes(ret);
        return { ret, std::string::npos };
    };

    fileError parseFromStream(inputStockSource& stream)
    {
        reset();
        {
            std::string line;
            stream.getLine(line);
            //Files may come from different OSs, so a manual check for CRLF must be done
            if (!line.empty() && line.back() == '\r')
                line.pop_back();

            constexpr std::array<char, 3> BOM{
                    static_cast<char>(0xEF), static_cast<char>(0xBB), static_cast<char>(0xBF) };

            //Verify fields against template
            static const std::array<std::string, 4> layout =
                    {
                            "Cost_Center",
                            "Name",
                            "Item_Size",
                            "BinNumber"
                    };
            size_t pos = 0;
            for (const auto i : BOM)
            {
                if (line[pos] != i)
                    return fileError::bad_BOM;
                pos++;
            }
            for (const auto& i : layout)
            {
                const auto data = getField(line, pos);
                if (data.first != i)
                    return fileError::bad_header;
                pos = data.second;
            }
            if (pos != std::string::npos)
                return fileError::bad_header;
        }
        std::string line;
        while (stream.getLine(line))
        {
            //Files may come from different OSs, so a manual check for CRLF must be done
            if (!line.empty() && line.back() == '\r')
                line.pop_back();
            if (line.empty())
                continue;
            auto discard = getField(line, 0);
            auto name = getField(line, discard.second);
            auto size = getField(line, name.second);

            stockElement elem;
            elem.name = std::move(name.first);
            elem.size = std::move(size.first);

            //Note that we don't need to standardise the size as it isn't a search parameter
            elements.push_back(std::move(elem));
        }

        tidy();
        return fileError::none;
    }

    fileError reuseFromStream(inputStockSource& stream)
    {
        reset();
        {
            std::string line;
            stream.getLine(line);
            //Files may come from different OSs, so a manual check for CRLF must be done
            if (!line.empty() && line.back() == '\r')
                line.pop_back();

            //Verify fields against template
            static const std::array<std::string, 3> layout =
                    {
                            "Name",
                            "Size",
                            "Total"
                    };
            size_t pos = 0;

            for (const auto& i : layout)
            {
                const auto data = getField(line, pos);
                if (data.first != i)
                    return fileError::bad_header;
                pos = data.second;
            }
            while (pos != std::string::npos)
            {
                auto [name, end] = getField(line, pos);
                pos = end;
                locationIndices.emplace_back(std::move(name));
            }
        }

        std::string line;
        while (stream.getLine(line))
        {
            //Files may come from different OSs, so a manual check for CRLF must be done
            if (!line.empty() && line.back() == '\r')
                line.pop_back();
            if (line.empty())
                continue;
            auto name = getField(line, 0);
            auto size = getField(line, name.second);
            auto discard = getField(line, size.second); //Discard global count, use subcounts instead

            stockElement elem;
            elem.name = std::move(name.first);
            elem.size = std::move(size.first);

            //Determine subcounts
            size_t pos = discard.second;
            for (size_t i = 0; i < locationIndices.size(); i++)
            {
                auto [val, end] = getField(line, pos);
                char* e = nullptr;
                float count = std::strtof(val.data(), &e);
                if (e == val.data())
                    return fileError::unable_to_parse_count;
                elem.counts.emplace_back(i, count);
                pos = end;
            }

            //Note that we don't need to standardise the size as it isn't a search parameter
            elements.push_back(std::move(elem));
        }

        tidy();
        return fileError::none;
    }

    void exportToStream(outputStockSource& stream, const bool skipEmpty) const
    {
        stream << "Name,Size,Total";
        for (const auto& i : locationIndices)
            stream << ',' << '\"' << i << '\"';
        stream << '\n';

        for (const auto& i : elements)
        {
            if (skipEmpty && i.sum() == 0)
                continue;

            stream << '\"' << i.name << '\"' << ',';
            stream << '\"' << i.size << '\"' << ',';
            stream << i.sum();

            for (size_t u = 0; u < locationIndices.size(); u++)
                stream << ',' << i.getCount(u);
            stream << '\n';
        }
    }

    inline static const std::string defaultLocation = "Global";

public:

    fileError loadFromFStream(const std::string& source)
    {
        std::ifstream stream(source);
        if (!stream)
            return fileError::unable_to_open;
        stream.seekg(0, std::ios::beg);

        inputStream sfs(stream);

        return parseFromStream(sfs);
    }

    fileError reuseFromFStream(const std::string& source)
    {
        std::ifstream stream;
        stream.open(source);
        if (!stream.is_open())
            return fileError::unable_to_open;
        stream.seekg(0, std::ios::beg);


        inputStream sfs(stream);

        return reuseFromStream(sfs);
    }

    fileError loadFromString(const std::string& source)
    {
        std::stringstream ss(source);
        inputStream sfs(ss);
        return parseFromStream(sfs);
    }

    fileError reuseFromString(const std::string& source)
    {
        std::stringstream ss(source);
        inputStream sfs(ss);
        return reuseFromStream(sfs);
    }

    fileError loadFromFILE(FILE* source)
    {
        if (source == nullptr)
            return fileError::unable_to_open;
        inputFILE sfs(source);

        return parseFromStream(sfs);
    }

    fileError reuseFromFILE(FILE* source)
    {
        if (source == nullptr)
            return fileError::unable_to_open;
        inputFILE sfs(source);

        return reuseFromStream(sfs);
    }

    auto getFullSearchList() const
    {
        return stockTableFullSearch(*this);
    }

    const stockElement& get(size_t idx) const
    {
        return elements[idx];
    }

    void setLocation(const std::string& locationName)
    {
        auto pos = std::find(locationIndices.cbegin(), locationIndices.cend(), locationName);
        if (pos == locationIndices.cend())
        {
            location = locationIndices.size();
            locationIndices.emplace_back(locationName);
        }
        else
        {
            location = pos - locationIndices.cbegin();
        }
    }

    void alter(size_t idx, float delta)
    {
        if (locationIndices.empty())
            locationIndices.push_back(defaultLocation);
        elements[idx].alter(location, delta);
    }

    bool exportToCSV(const std::string& file, bool skipEmpty) const
    {
        std::ofstream out;
        out.open(file, std::ios::trunc);

        if (!out.is_open())
            return false;

        outputStream stream{ out };

        exportToStream(stream, skipEmpty);

        return true;
    }

    bool exportToCSV(FILE* file, bool skipEmpty) const
    {
        if (file == nullptr)
            return false;
        outputFILE stream{ file };
        exportToStream(stream, skipEmpty);
        return true;
    }

    size_t size() const { return elements.size(); }

    std::string exportToString(bool skipEmpty) const
    {
        std::stringstream ss;
        outputStream stream{ ss };
        exportToStream(stream, skipEmpty);
        return ss.str();
    }

    size_t getCurrentLocation() const
    {
        if (locationIndices.empty())
            return std::numeric_limits<size_t>::max();
        return location;
    }

    const std::string& getLocation(size_t idx) const
    {
        return locationIndices[idx];
    }

    size_t getLocationCount() const
    {
        return locationIndices.size();
    }

    bool hasLocation() const
    {
        return !locationIndices.empty();
    }

    bool hasContent() const
    {
        return !elements.empty();
    }

    static constexpr auto getErrorString(fileError code)
    {
        switch (code)
        {
            default:
                return "Bad error code.";
            case(fileError::none):
                return "No error.";
            case(fileError::unable_to_open):
                return "Unable to open file.";
            case(fileError::bad_BOM):
                return "Invalid BOM.";
            case(fileError::bad_header):
                return "Invalid header layout.";
            case(fileError::unable_to_parse_count):
                return "File contained invalid count value.";
        }
    }

    const std::vector<stockElement>& getAll() const
    {
        return elements;
    }
};
