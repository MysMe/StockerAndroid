#include "StockerNatives/interface/stockTableInterface.h"
#include "Export.h"
#include <jni.h>

extern "C"
JNIEXPORT jptr JNICALL
Java_com_example_cppapp_StockTable_NativeNew(JNIEnv*, jobject ) {
    return getJLong(stockTable_new());
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_cppapp_StockTable_NativeDelete(JNIEnv*, jobject , jptr ptr) {
    stockTable_delete(getPtr<stockTable>(ptr));
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_cppapp_StockTable_NativeImport(JNIEnv *env, jobject , jptr ptr,
                                                jstring source) {
    const auto src = jstring2string(env, source);
    auto file = fopen_fd(src.c_str(), "r");
    return stockTable_import_from_FILE(getPtr<stockTable>(ptr), file.fptr);
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_cppapp_StockTable_NativeReuse(JNIEnv *env, jobject , jptr ptr,
                                               jstring source) {
    const auto src = jstring2string(env, source);
    auto file = fopen_fd(src.c_str(), "r");
    return stockTable_reuse_from_FILE(getPtr<stockTable>(ptr), file.fptr);
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_cppapp_StockTable_NativeImportFromString(JNIEnv *env, jobject , jptr ptr,
                                                          jstring data) {
    const auto str = jstring2string(env, data);
    return stockTable_import_from_string(getPtr<stockTable>(ptr), str.c_str());
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_cppapp_StockTable_NativeReuseFromString(JNIEnv *env, jobject , jptr ptr,
                                                         jstring data) {
    const auto str = jstring2string(env, data);
    return stockTable_reuse_from_string(getPtr<stockTable>(ptr), str.c_str());
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_cppapp_StockTable_NativeGetImportError(JNIEnv *env, jobject , jint error) {
    return env->NewStringUTF(stockTable_getImportError(error));
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_cppapp_StockTable_NativeSetLocation(JNIEnv *env, jobject , jptr ptr,
                                                     jstring location) {
    const auto str = jstring2string(env, location);
    stockTable_setLocation(getPtr<stockTable>(ptr), str.c_str());
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_cppapp_StockTable_NativeLocationCount(JNIEnv*, jobject , jptr ptr) {
    return stockTable_getLocationCount(getPtr<stockTable>(ptr));
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_cppapp_StockTable_NativeGetCurrentLocation(JNIEnv*, jobject , jptr ptr) {
    return getPtr<stockTable>(ptr)->getCurrentLocation();
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_cppapp_StockTable_NativeGetLocationName(JNIEnv *env, jobject , jptr ptr, jint index) {
    const auto str = getPtr<stockTable>(ptr)->getLocation(index);
    return env->NewStringUTF(str.c_str());
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_cppapp_StockTable_NativeGetStockName(JNIEnv *env, jobject , jptr ptr,
                                                      jint index) {
    return env->NewStringUTF(stockTable_getStockName(getPtr<stockTable>(ptr), index));
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_cppapp_StockTable_NativeGetStockSize(JNIEnv *env, jobject , jptr ptr,
                                                      jint index) {
    return env->NewStringUTF(stockTable_getStockSize(getPtr<stockTable>(ptr), index));
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_cppapp_StockTable_NativeAlterStock(JNIEnv*, jobject , jptr ptr,
                                                    jint index, jfloat delta) {
    stockTable_alter(getPtr<stockTable>(ptr), index, delta);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_cppapp_StockTable_NativeExportToFile(JNIEnv *env, jobject , jptr ptr,
                                                      jstring source, jboolean min) {
    const auto str = jstring2string(env, source);
    auto file = fopen_fd(str.c_str(), "w");
    stockTable_export_FILE(getPtr<stockTable>(ptr), file.fptr, min);
}
extern "C"
JNIEXPORT jfloat JNICALL
Java_com_example_cppapp_StockTable_NativeGetStockCount(JNIEnv*, jobject , jptr ptr,
                                                       jint index) {
    return stockTable_getStockCount(getPtr<stockTable>(ptr), index);
}
extern "C"
JNIEXPORT jfloat JNICALL
Java_com_example_cppapp_StockTable_NativeGetStockCountAt(JNIEnv*, jobject, jlong ptr,
                                                         jint index, jint location) {
    return getPtr<stockTable>(ptr)->get(index).getCount(location);
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_cppapp_StockTable_NativeExportToString(JNIEnv *env, jobject, jlong ptr,
                                                        jboolean min) {
    const auto str = getPtr<stockTable>(ptr)->exportToString(min);
    return env->NewStringUTF(str.c_str());
}