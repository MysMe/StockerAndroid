#include "LanCon/interface/UDPSenderInterface.h"
#include "Export.h"
#include <jni.h>

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_cppapp_UDPSender_NativeNew(JNIEnv*, jobject, jchar port) {
    return getJLong(UDPSender_new(port));
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_cppapp_UDPSender_NativeDelete(JNIEnv*, jobject, jlong ptr) {
    UDPSender_delete(getPtr<UDPSender>(ptr));
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_cppapp_UDPSender_NativeRequestAddress(JNIEnv*, jobject, jlong ptr) {
    UDPSender_request_addresses(getPtr<UDPSender>(ptr));
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_cppapp_UDPSender_NativeRequestLink(JNIEnv *env, jobject, jlong ptr,
                                                    jstring target, jchar port) {
    const auto str = jstring2string(env, target);
    UDPSender_request_link(getPtr<UDPSender>(ptr), str.c_str(), port);
}


extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_cppapp_UDPSender_NativeAwaitMessage(JNIEnv*, jobject, jlong ptr,
                                                     jchar timeout_ms) {
    return getJLong(UDPSender_await_response_new(getPtr<UDPSender>(ptr), timeout_ms));
}