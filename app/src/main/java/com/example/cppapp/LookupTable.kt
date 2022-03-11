package com.example.cppapp

class LookupResult constructor(private val ptr: pointer)
{
    private external fun NativeSize(ptr: pointer): Int
    private external fun NativeGet(ptr: pointer, index: Int): Int
    private external fun NativeDelete(ptr: pointer)

    fun finalize()
    {
        NativeDelete(ptr)
    }

    fun size(): Int
    {
        return NativeSize(ptr)
    }

    operator fun get(index: Int): Int
    {
        return NativeGet(ptr, index)
    }
}

class LookupTable(table: StockTable) {
    private var ptr: pointer = 0
    private external fun NativeNew(table: pointer): pointer
    private external fun NativeDelete(ptr: pointer)
    private external fun NativeSearch(table: pointer, searchTerm: String): pointer

    init {
        ptr = NativeNew(table.Ptr())
    }

    fun finalize()
    {
        NativeDelete(ptr)
    }

    fun search(searchTerm: String): LookupResult
    {
        return LookupResult(NativeSearch(ptr, searchTerm))
    }
}