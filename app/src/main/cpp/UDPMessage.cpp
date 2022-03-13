#include "LanCon/interface/UDPMessageInterface.h"
#include "Export.h"
#include <jni.h>

extern "C"
JNIEXPORT void JNICALL
Java_com_example_cppapp_UDPMessage_NativeDelete(JNIEnv*, jobject, jlong ptr) {
    UDPMessage_delete(getPtr<UDPMessage>(ptr));
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_cppapp_UDPMessage_NativeAddress(JNIEnv *env, jobject, jlong ptr) {
    //Note that we bypass the C interface saving a copy (str->cstr->kotlin str instead of str->kotlin str)
    const auto str = getPtr<UDPMessage>(ptr)->endpoint.address().to_string();
    return env->NewStringUTF(str.c_str());
}
extern "C"
JNIEXPORT jbyte JNICALL
Java_com_example_cppapp_UDPMessage_NativeRequest(JNIEnv*, jobject, jlong ptr) {
    return UDPMessage_request(getPtr<UDPMessage>(ptr));
}
extern "C"
JNIEXPORT jchar JNICALL
Java_com_example_cppapp_UDPMessage_NativeAdditional(JNIEnv*, jobject, jlong ptr) {
    return UDPMessage_additional(getPtr<UDPMessage>(ptr));
}
extern "C"
JNIEXPORT jbyte JNICALL
Java_com_example_cppapp_UDPMessage_requestConfirmLink(JNIEnv*, jobject) {
    return static_cast<uint8_t>(UDPRequest::acceptLink);
}
extern "C"
JNIEXPORT jbyte JNICALL
Java_com_example_cppapp_UDPMessage_requestDenyLink(JNIEnv*, jobject) {
    return static_cast<uint8_t>(UDPRequest::denyLink);
}