#include "lookupTable.h"
#include "stockTable.h"

//Generic function for any table given a number of elements and a function for getting element names
template <class Ta, class Tg>
void generate_generic(lookupTable& out, const Ta& table, std::size_t elems, Tg getter)
{
    for (std::size_t i = 0; i < elems; i++)
    {
        const auto& name = getter(table, i);

        auto skipInvalid = [&](auto& it)
        {
            while (it != name.cend() && !std::isalnum(static_cast<unsigned char>(*it)))
                ++it;
        };

        auto insert = [&](const auto lit, const auto rit)
        {
            //TODO: Can be updated to a string view if supported for map lookups (C++20)
            const std::string str{ lit, rit };
            out.table[str].push_back(i);
        };

        //Divide on non-alphanumeric characters, but ignore continuous sequences of alphanumeric characters

        auto left = name.cbegin();
        auto pos = name.cbegin();
        while (pos != name.cend())
        {
            skipInvalid(left);
            pos = std::find_if(left, name.cend(),
                [](const char v) {return !std::isalnum(static_cast<unsigned char>(v)); });
            insert(left, pos);
            left = pos + (pos != name.cend());
        }
    }
}

lookupTable::lookupTable(const stockTable& table)
{
    generate_generic(*this, table, table.size(),
        [](const stockTable& table, size_t idx) {return table.get(idx).name; });
}

std::vector<size_t> lookupTable::splitSearch(const std::string& keywords) const
{
    std::vector<std::string> substrings;

    size_t pos = 0;
    while (pos != std::string::npos)
    {
        size_t end = keywords.find(' ', pos);
        substrings.emplace_back(keywords.substr(pos, end - pos));
        pos = end + (end != std::string::npos);
    }

    return search(substrings);
}

std::vector<size_t> lookupTable::search(const std::vector<std::string>& keywords) const
{
    if (keywords.empty())
        return{};

    std::vector<
        std::pair<
        decltype(table)::const_iterator,
        decltype(table)::const_iterator>> lookups;

    for (const auto& i : keywords)
    {
        std::string upper = i;
        stringToUpper(upper);

        auto lower = table.lower_bound(upper);
        auto end = lower;
        //Iterate forwards until the prefix doesn't match
        //Note we specifically use the manually sized overload to prevent it wrongly comparing longer strings
        while (end != table.cend() && end->first.compare(0, upper.size(), upper) == 0)
            ++end;
        lookups.emplace_back(lower, end);
    }

    auto reduce = [&](decltype(lookups)::value_type range)
    {
        std::vector<size_t> reduction;

        //Minimum number of elements, there will likely be more
        reduction.reserve(std::distance(range.first, range.second));
        for (auto it = range.first; it != range.second; ++it)
        {
            for (const auto i : it->second)
            {
                reduction.push_back(i);
            }
        }
        std::sort(reduction.begin(), reduction.end());
        reduction.erase(std::unique(reduction.begin(), reduction.end()), reduction.end());
        return reduction;
    };

    std::vector<std::vector<size_t>> reductions;
    reductions.reserve(lookups.size());
    std::transform(lookups.cbegin(), lookups.cend(), std::back_inserter(reductions), reduce);

    for (auto it = reductions.cbegin() + 1; it != reductions.cend(); ++it)
    {
        reductions[0].erase(
            std::set_intersection(reductions[0].cbegin(), reductions[0].cend(), it->cbegin(), it->cend(), reductions[0].begin()),
            reductions[0].end());
    }
    return reductions[0];
}
