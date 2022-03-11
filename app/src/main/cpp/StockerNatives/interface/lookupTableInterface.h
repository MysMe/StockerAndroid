#pragma once
#include "Export.h"
#include "../include/lookupTable.h"

STOCKEREXPORT lookupTable* lookupTable_new_from_stockTable(stockTable* table)
{
	if (!table)
		return nullptr;

	return new lookupTable(*table);
}

STOCKEREXPORT void lookupTable_delete(lookupTable* table)
{
	delete table;
}

STOCKEREXPORT std::vector<size_t>* lookupTable_search_new(lookupTable* table, const char* const searchTerm)
{
	return new std::vector<size_t>(std::move(table->splitSearch(searchTerm)));
}

STOCKEREXPORT int lookupTable_search_size(std::vector<size_t>* vals)
{
	return static_cast<int>(vals->size());
}

STOCKEREXPORT int lookupTable_search_value(std::vector<size_t>* vals, int index)
{
	return static_cast<int>((*vals)[index]);
}

STOCKEREXPORT void lookupTable_search_delete(std::vector<size_t>* vals)
{
	delete vals;
}