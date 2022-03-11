#pragma once
#include <map>
#include <string>
#include <vector>
#include <algorithm>
#include <cstddef>

class stockTable;
class unifiedStockTable;


class lookupTable
{
	std::map<std::string, std::vector<std::size_t>> table;

	lookupTable() = default;

	static void stringToUpper(std::string& str)
	{
		std::transform(str.begin(), str.end(), str.begin(), [](unsigned char c) -> unsigned char { return static_cast<unsigned char>(std::toupper(c)); });
	}

	template <class Ta, class Tg>
	friend void generate_generic(lookupTable& out, const Ta&, std::size_t, Tg);

public:
	lookupTable(const stockTable& table);
	lookupTable(const unifiedStockTable& table);

	lookupTable(const lookupTable&) = default;
	lookupTable(lookupTable&&) = default;
	lookupTable& operator=(const lookupTable&) = default;
	lookupTable& operator=(lookupTable&&) = default;

    std::vector<size_t> splitSearch(const std::string& keywords) const;

	std::vector<size_t> search(const std::vector<std::string>& keywords) const;
};