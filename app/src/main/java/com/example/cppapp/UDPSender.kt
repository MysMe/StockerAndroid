package com.example.cppapp

class UDPSender : AutoCloseable {

    private var ptr: pointer = 0

    private external fun NativeNew(port: Char): pointer
    private external fun NativeDelete(ptr: pointer)


    private external fun NativeRequestAddress(ptr: pointer)
    private external fun NativeRequestLink(ptr: pointer, target: String, port: Char)
    private external fun NativeAwaitMessage(ptr: pointer, timeoutMS: Char): pointer

    constructor(port: Char) {
        ptr = NativeNew(port)
    }

    fun broadcastAddressRequest()
    {
        NativeRequestAddress(ptr)
    }

    fun sendLinkRequest(target: String, port: Char)
    {
        NativeRequestLink(ptr, target, port)
    }

    fun awaitMessage(timeoutMS: Char): UDPMessage
    {
        return UDPMessage(NativeAwaitMessage(ptr, timeoutMS))
    }

    override fun close() {
        NativeDelete(ptr)
    }
}