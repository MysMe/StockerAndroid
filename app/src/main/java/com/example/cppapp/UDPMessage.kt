package com.example.cppapp

class UDPMessage constructor(private var ptr: pointer) : AutoCloseable {

    private external fun NativeDelete(ptr: pointer)

    private external fun NativeAddress(ptr: pointer): String
    private external fun NativeRequest(ptr: pointer): Byte
    private external fun NativeAdditional(ptr: pointer): Char

    external fun requestConfirmLink(): Byte
    external fun requestDenyLink(): Byte

    fun address(): String {return NativeAddress(ptr)}
    fun request(): Byte {return NativeRequest(ptr)}
    fun additional(): Char {return NativeAdditional(ptr)}

    fun valid(): Boolean {return ptr != 0L}
    override fun close() {
        NativeDelete(ptr)
    }
}