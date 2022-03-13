package com.example.cppapp

class TCPSender : AutoCloseable {

    private var ptr: pointer = 0

    private external fun NativeNew(): pointer
    private external fun NativeDelete(ptr: pointer)


    private external fun NativeConnect(ptr: pointer, address: String, port: Char, timeoutMS: Char): Boolean
    private external fun NativeConnected(ptr: pointer): Boolean

    private external fun NativeDisconnect(ptr: pointer)

    private external fun NativeSend(ptr: pointer, message: String)

    init {
        ptr = NativeNew()
    }

    fun connect(address: String, port: Char, timeoutMS: Char): Boolean
    {
        return NativeConnect(ptr, address, port, timeoutMS)
    }

    fun connected(): Boolean
    {
        return NativeConnected(ptr)
    }

    fun disconnect()
    {
        NativeDisconnect(ptr)
    }

    fun send(message: String)
    {
        NativeSend(ptr, message)
    }

    override fun close() {
        NativeDisconnect(ptr)
        NativeDelete(ptr)
    }
}