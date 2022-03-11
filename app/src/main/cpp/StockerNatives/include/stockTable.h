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

    class stockSource
    {
    public:
        virtual stockSource& getLine(std::string& out) = 0;
        virtual operator bool() const = 0;
    };

    class stockFStream final : public stockSource
    {
        std::reference_wrapper<std::istream> stream;
    public:

        stockFStream(std::istream& source) : stream(source) {}

        stockSource& getLine(std::string& out) final
        {
            std::getline(stream.get(), out);
            return *this;
        }
        operator bool() const final
        {
            return static_cast<bool>(stream.get());
        }
    };

    class stockFILE final : public stockSource
    {
        FILE* ptr;
    public:

        stockFILE() = delete;
        stockFILE(FILE* src) : ptr(src) {}

        stockSource& getLine(std::string& out) final
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

    static void stringToUpper(std::string& str)
    {
        std::transform(str.begin(), str.end(), str.begin(), [](unsigned char c) -> unsigned char { return static_cast<unsigned char>(std::toupper(c)); });
    }

    std::vector<stockElement> elements;

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

        //Sort into consistent order
        std::sort(elements.begin(), elements.end());
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
private:

    static auto getField(std::string line, size_t off) -> std::pair<std::string, size_t>
    {
        bool isQuoted = false;
        for (size_t i = off; i < line.size(); i++)
        {
            if (line[i] == '\"')
                isQuoted = !isQuoted;
            if (line[i] == ',' && !isQuoted)
            {
                return { line.substr(off, i - off), i == line.size() - 1 ? std::string::npos : i + 1 };
            }
        }

        return { line.substr(off, line.size() - off), std::string::npos };
    };

    fileError parseFromStream(stockSource& stream)
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

    fileError reuseFromStream(stockSource& stream)
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
                "Quantity"
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

    inline static const std::string defaultLocation = "Global";

public:

    fileError loadFromFStream(const std::string& source)
    {
        std::ifstream stream(source, std::ios::in);
        stream.seekg(0, std::ios::beg);

        stockFStream sfs(stream);

        auto v = parseFromStream(sfs);
        if (v != fileError::none)
            return v;

        return fileError::none;
    }

    fileError reuseFromFStream(const std::string& source)
    {
        std::ifstream stream;
        stream.open(source, std::ios::in);
        if (!stream.is_open())
            return fileError::unable_to_open;
        stream.seekg(0, std::ios::beg);


        stockFStream sfs(stream);

        auto v = reuseFromStream(sfs);
        if (v != fileError::none)
            return v;

        return fileError::none;
    }

    fileError loadFromString(const std::string& source)
    {
        std::stringstream ss(source);
        stockFStream sfs(ss);
        parseFromStream(sfs);
        return fileError::none;
    }

    fileError reuseFromString(const std::string& source)
    {
        std::stringstream ss(source);
        stockFStream sfs(ss);
        reuseFromStream(sfs);
        return fileError::none;
    }

    fileError loadFromFILE(FILE* source)
    {
        if (source == nullptr)
            return fileError::unable_to_open;
        stockFILE sfs(source);

        auto v = parseFromStream(sfs);
        if (v != fileError::none)
            return v;

        return fileError::none;
    }

    fileError reuseFromFILE(FILE* source)
    {
        if (source == nullptr)
            return fileError::unable_to_open;
        stockFILE sfs(source);

        auto v = reuseFromStream(sfs);
        if (v != fileError::none)
            return v;

        return fileError::none;
    }

    auto fullSearch(const std::string& keyword) const
    {
        struct comp
        {
            bool operator()(const stockElement& elem, const std::string& str) const
            {
                return elem.name < str;
            }
            bool operator()(const std::string& str, const stockElement& elem) const
            {
                return str < elem.name;
            }
        };
        return std::equal_range(elements.cbegin(), elements.cend(), keyword, comp());
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

        out << "Name,Size,Quantity";
        for (const auto& i : locationIndices)
            out << ',' << i;
        out << '\n';

        for (const auto& i : elements)
        {
            if (skipEmpty && i.sum() == 0)
                continue;

            out << i.name << ',';
            out << i.size << ',';
            out << std::to_string(i.sum());

            if (locationIndices.empty())
            {
                out << ',' << i.sum();
            }
            else
                {
                for (size_t u = 0; u < locationIndices.size(); u++)
                    out << ',' << i.getCount(u);
            }
            out << '\n';
        }
        return true;
    }

    bool exportToCSV(FILE* file, bool skipEmpty) const
    {
        if (file == nullptr)
            return false;

        std::fputs("Name,Size,Quantity", file);
        for (const auto &i : locationIndices)
        {
            std::fputc(',', file);
            std::fputs(i.c_str(), file);
        }
        std::fputc('\n', file);

        for (const auto& i : elements)
        {
            if (skipEmpty && i.sum() == 0)
                continue;

            std::fputs(i.name.c_str(), file);
            std::fputc(',', file);
            std::fputs(i.size.c_str(), file);
            std::fputc(',', file);
            std::fputs(std::to_string(i.sum()).c_str(), file);

            if (!locationIndices.empty())
            {
                for (size_t u = 0; u < locationIndices.size(); u++)
                {
                    std::fputc(',', file);
                    std::fputs(std::to_string(i.getCount(u)).c_str(), file);
                }
            }
            std::fputc('\n', file);
        }
        return true;
    }

    size_t size() const { return elements.size(); }

    std::string exportToString(bool skipEmpty) const
    {
        std::string ret;
        ret += "Name,Size,Quantity";
        for (const auto& i : locationIndices)
            ret += (',' + i);
        ret += '\n';

        for (const auto& i : elements)
        {
            if (skipEmpty && i.sum() == 0)
                continue;

            ret += i.name + ',';
            ret += i.size + ',';
            ret += std::to_string(i.sum());

            for (size_t u = 0; u < locationIndices.size(); u++)
                ret += ',' + std::to_string(i.getCount(u));
            ret += '\n';
        }
        return ret;
    }

    const std::string& getCurrentLocation() const
    {
        if (locationIndices.empty())
            return defaultLocation;
        return locationIndices[location];
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
