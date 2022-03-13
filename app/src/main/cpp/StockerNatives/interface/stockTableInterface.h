#pragma once
#include "Export.h"
#include "../include/stockTable.h"

STOCKEREXPORT stockTable* stockTable_new()
{
    return new stockTable;
}

STOCKEREXPORT void stockTable_delete(stockTable* obj)
{
    delete obj;
}

STOCKEREXPORT int stockTable_import(stockTable* obj, char const * const source)
{
    return static_cast<int>(obj->loadFromFStream(source));
}

STOCKEREXPORT int stockTable_reuse(stockTable* obj, char const* const source)
{
    return static_cast<int>(obj->reuseFromFStream(source));
}

STOCKEREXPORT int stockTable_import_from_string(stockTable* obj, char const* const data)
{
    return static_cast<int>(obj->loadFromString(data));
}

STOCKEREXPORT int stockTable_reuse_from_string(stockTable* obj, char const* const data)
{
    return static_cast<int>(obj->reuseFromString(data));
}

STOCKEREXPORT int stockTable_import_from_FILE(stockTable* obj, FILE* data)
{
    return static_cast<int>(obj->loadFromFILE(data));
}

STOCKEREXPORT int stockTable_reuse_from_FILE(stockTable* obj, FILE* data)
{
    return static_cast<int>(obj->reuseFromFILE(data));
}

STOCKEREXPORT void stockTable_setLocation(stockTable* obj, char const * const location)
{
    obj->setLocation(location);
}

STOCKEREXPORT const char* stockTable_getStockName(stockTable* obj, unsigned int index)
{
    return obj->get(index).name.c_str();
}

STOCKEREXPORT void stockTable_alter(stockTable* obj, unsigned int index, float delta)
{
    obj->alter(index, delta);
}

STOCKEREXPORT void stockTable_export(stockTable* obj, char const* const location, bool minimal)
{
    obj->exportToCSV(location, minimal);
}

STOCKEREXPORT void stockTable_export_FILE(stockTable* obj, FILE* file, bool minimal)
{
    obj->exportToCSV(file, minimal);
}

STOCKEREXPORT const char* stockTable_getStockSize(stockTable* obj, unsigned int index)
{
    return obj->get(index).size.c_str();
}


STOCKEREXPORT float stockTable_getStockCount(stockTable* obj, unsigned int index)
{
    return obj->get(index).sum();
}

STOCKEREXPORT unsigned int stockTable_getCurrentLocation(stockTable* obj)
{
    return obj->getCurrentLocation();
}

STOCKEREXPORT unsigned int stockTable_getLocationCount(stockTable* obj)
{
    return obj->getLocationCount();
}

STOCKEREXPORT const char* stockTable_getLocation(stockTable* obj, unsigned int index)
{
    return obj->getLocation(index).c_str();
}

STOCKEREXPORT bool stockTable_hasLocations(stockTable* obj)
{
    return obj->hasLocation();
}

STOCKEREXPORT const char* stockTable_getImportError(int code)
{
    return stockTable::getErrorString(static_cast<stockTable::fileError>(code));
}

STOCKEREXPORT bool stockTable_hasContent(stockTable* obj)
{
    return obj->hasContent();
}
