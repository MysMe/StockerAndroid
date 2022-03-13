#include "LanCon/interface/TCPSenderInterface.h"
#include "Export.h"
#include <jni.h>


extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_cppapp_TCPSender_NativeNew(JNIEnv*, jobject) {
    return getJLong(TCPSender_new());
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_cppapp_TCPSender_NativeDelete(JNIEnv*, jobject, jlong ptr) {
    TCPSender_delete(getPtr<TCPSender>(ptr));
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_cppapp_TCPSender_NativeConnect(JNIEnv *env, jobject, jlong ptr,
                                                 jstring address, jchar port,
                                                 jchar timeout_ms) {
    const auto addr = jstring2string(env, address);
    return TCPSender_connect(getPtr<TCPSender>(ptr), addr.c_str(), port, timeout_ms);
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_cppapp_TCPSender_NativeConnected(JNIEnv*, jobject, jlong ptr) {
    return TCPSender_connected(getPtr<TCPSender>(ptr));
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_cppapp_TCPSender_NativeDisconnect(JNIEnv*, jobject, jlong ptr) {
    TCPSender_disconnect(getPtr<TCPSender>(ptr));
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_cppapp_TCPSender_NativeSend(JNIEnv *env, jobject, jlong ptr,
                                             jstring message) {
    const auto str = jstring2string(env, message);
    TCPSender_send(getPtr<TCPSender>(ptr), str.c_str());
}