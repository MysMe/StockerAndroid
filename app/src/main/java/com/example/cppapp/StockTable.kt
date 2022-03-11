package com.example.cppapp

typealias pointer = Long

class StockTable() {

    private var ptr: pointer = 0
    private external fun NativeNew(): pointer
    private external fun NativeDelete(ptr: pointer)

    private external fun NativeImport(ptr: pointer, source: String): Int
    private external fun NativeReuse(ptr: pointer, source: String): Int
    private external fun NativeImportFromString(ptr: pointer, data: String): Int
    private external fun NativeReuseFromString(ptr: pointer, data: String): Int
    private external fun NativeGetImportError(error: Int): String

    private external fun NativeSetLocation(ptr: pointer, location: String)
    private external fun NativeLocationCount(ptr: pointer): Int
    private external fun NativeGetCurrentLocation(ptr: pointer): String
    private external fun NativeGetLocationName(ptr: pointer, index: Int): String

    private external fun NativeGetStockName(ptr: pointer, index: Int): String
    private external fun NativeGetStockSize(ptr: pointer, index: Int): String
    private external fun NativeAlterStock(ptr: pointer, index: Int, delta: Float)
    private external fun NativeGetStockCount(ptr: pointer, index: Int): Float

    private external fun NativeExportToFile(ptr: pointer, source: String, min: Boolean)

    init {
        ptr = NativeNew()
    }

    fun finalize()
    {
        NativeDelete(ptr)
    }

    fun Ptr(): pointer
    {
        return ptr
    }

    fun importFromFile(source: String): Int {return NativeImport(ptr, source)}
    fun reuseFromFile(source: String): Int {return NativeReuse(ptr, source)}
    fun importFromData(source: String): Int {return NativeImportFromString(ptr, source)}
    fun reuseFromData(source: String): Int {return NativeReuseFromString(ptr, source)}

    fun getImportError(ec: Int): String {return NativeGetImportError(ec)}

    //Returns true if a new location was added, false otherwise
    fun setLocation(location: String): Boolean
    {
        val size = getLocationCount()
        NativeSetLocation(ptr, location)
        return size != getLocationCount()
    }

    fun getLocationCount(): Int {return NativeLocationCount(ptr)}
    fun getCurrentLocation(): String {return NativeGetCurrentLocation(ptr)}
    fun getLocationName(index: Int): String {return NativeGetLocationName(ptr, index)}

    fun getStockName(index: Int): String {return NativeGetStockName(ptr, index)}
    fun getStockSize(index: Int): String {return NativeGetStockSize(ptr, index)}
    fun alter(index: Int, delta: Float) {NativeAlterStock(ptr, index, delta)}
    fun getStockCount(index: Int): Float {return NativeGetStockCount(ptr, index)}

    fun exportToFile(source: String, min: Boolean) {NativeExportToFile(ptr, source, min)}
}