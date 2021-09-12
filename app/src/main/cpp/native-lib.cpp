#include <jni.h>
#include <string>
#include <D:\Code\StockerBackend\StockerBackend\stockTable.h>


stockTable table;

std::string jstring2string(JNIEnv *env, jstring jStr) {
    if (!jStr)
        return "";

    const jclass stringClass = env->GetObjectClass(jStr);
    const jmethodID getBytes = env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
    const jbyteArray stringJbytes = (jbyteArray) env->CallObjectMethod(jStr, getBytes, env->NewStringUTF("UTF-8"));

    size_t length = (size_t) env->GetArrayLength(stringJbytes);
    jbyte* pBytes = env->GetByteArrayElements(stringJbytes, NULL);

    std::string ret = std::string((char *)pBytes, length);
    env->ReleaseByteArrayElements(stringJbytes, pBytes, JNI_ABORT);

    env->DeleteLocalRef(stringJbytes);
    env->DeleteLocalRef(stringClass);
    return ret;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_cppapp_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++, modified";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_cppapp_MainActivity_importCSV(JNIEnv *env, jobject thiz, jstring CSV) {
    return static_cast<int>(table.loadFromString(jstring2string(env, CSV)));
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_cppapp_MainActivity_addLocation(JNIEnv *env, jobject thiz) {
    // TODO: implement setLocation()
}
extern "C"
JNIEXPORT jintArray JNICALL
Java_com_example_cppapp_SearchResultActivity_searchStock(JNIEnv *env, jobject thiz, jstring searchTerm) {

    const auto IDs = table.splitSearch(jstring2string(env, searchTerm));

    jintArray result;
    result = env->NewIntArray(IDs.size());
    if (result == nullptr)
        return nullptr;

    std::vector<jint> arr;
    arr.reserve(IDs.size());
    std::transform(IDs.cbegin(), IDs.cend(), std::back_inserter(arr), [&](size_t v)->jint{return v;});
    env->SetIntArrayRegion(result, 0, IDs.size(), arr.data());
    return result;
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_cppapp_SearchResultActivity_getStockName(JNIEnv *env, jobject thiz, jint index) {
    std::string name = table.get(index).name;
    return env->NewStringUTF(name.c_str());
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_cppapp_AlterStockActivity_alterStock(JNIEnv *env, jobject thiz, jint index, jfloat delta) {
    table.alter(index, delta);
    return nullptr;
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_cppapp_MainActivity_exportCSV(JNIEnv *env, jobject thiz, jstring CSV, jboolean minimal) {
    table.exportToCSV(jstring2string(env, CSV), minimal);
    return nullptr;
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_cppapp_SearchResultActivity_getStockSize(JNIEnv *env, jobject thiz, jint index) {
    std::string size = table.get(index).size;
    return env->NewStringUTF(size.c_str());
}
extern "C"
JNIEXPORT jfloat JNICALL
Java_com_example_cppapp_AlterStockActivity_getStockCount(JNIEnv *env, jobject thiz, jint index) {
    return table.get(index).sum();
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_cppapp_MainActivity_setStockLocation(JNIEnv *env, jobject thiz, jstring location) {
    table.setLocation(jstring2string(env, location));
    return nullptr;
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_cppapp_MainActivity_getStockLocation(JNIEnv *env, jobject thiz) {
    return env->NewStringUTF(table.getCurrentLocation().c_str());
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_cppapp_AlterStockActivity_getStockLocation(JNIEnv *env, jobject thiz) {
    return env->NewStringUTF(table.getCurrentLocation().c_str());
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_cppapp_MainActivity_hasStockLocation(JNIEnv *env, jobject thiz) {
    return table.hasLocation();
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_cppapp_MainActivity_reimportCSV(JNIEnv *env, jobject thiz, jstring CSV) {
    return static_cast<int>(table.reuseFromString(jstring2string(env, CSV)));
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_cppapp_MainActivity_getImportError(JNIEnv *env, jobject thiz, jint ec) {
    return env->NewStringUTF(stockTable::getErrorString(static_cast<stockTable::fileError>(ec)));
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_cppapp_MainActivity_getStockLocationFromID(JNIEnv *env, jobject thiz, jint id) {
    return env->NewStringUTF(table.getLocation(id).c_str());
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_cppapp_MainActivity_getStockLocationCount(JNIEnv *env, jobject thiz) {
    return table.getLocationCount();
}