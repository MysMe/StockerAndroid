#include "StockerNatives/interface/lookupTableInterface.h"
#include "Export.h"
#include <jni.h>

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_cppapp_LookupResult_NativeSize(JNIEnv *env, jobject thiz, jptr ptr) {
    return lookupTable_search_size(getPtr<std::vector<size_t>>(ptr));
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_cppapp_LookupResult_NativeGet(JNIEnv *env, jobject thiz, jptr ptr, jint index) {
    return lookupTable_search_value(getPtr<std::vector<size_t>>(ptr), index);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_cppapp_LookupResult_NativeDelete(JNIEnv *env, jobject thiz, jptr ptr) {
    lookupTable_search_delete(getPtr<std::vector<size_t>>(ptr));
}
extern "C"
JNIEXPORT jptr JNICALL
Java_com_example_cppapp_LookupTable_NativeNew(JNIEnv *env, jobject thiz, jptr table) {
    return getJLong(lookupTable_new_from_stockTable(getPtr<stockTable>(table)));
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_cppapp_LookupTable_NativeDelete(JNIEnv *env, jobject thiz, jptr ptr) {
    lookupTable_delete(getPtr<lookupTable>(ptr));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_cppapp_LookupTable_NativeSearch(JNIEnv *env, jobject thiz, jptr table,
                                                 jstring search_term) {
    const auto str = jstring2string(env, search_term);
    return getJLong(lookupTable_search_new(getPtr<lookupTable>(table), str.c_str()));
}