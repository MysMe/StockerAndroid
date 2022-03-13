#ifndef CPPAPP_EXPORT_H
#define CPPAPP_EXPORT_H
#define EXPORT(X) extern "C" JNIEXPORT X JNICALL
#include <string>
#include <jni.h>
inline std::string jstring2string(JNIEnv *env, jstring jStr) {
    if (!jStr)
        return "";

    const jclass stringClass = env->GetObjectClass(jStr);
    const jmethodID getBytes = env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
    const jbyteArray stringJbytes = (jbyteArray) env->CallObjectMethod(jStr, getBytes, env->NewStringUTF("UTF-8"));

    size_t length = (size_t) env->GetArrayLength(stringJbytes);
    jbyte* pBytes = env->GetByteArrayElements(stringJbytes, nullptr);

    std::string ret = std::string((char *)pBytes, length);
    env->ReleaseByteArrayElements(stringJbytes, pBytes, JNI_ABORT);

    env->DeleteLocalRef(stringJbytes);
    env->DeleteLocalRef(stringClass);
    return ret;
}

using jptr = jlong;

template <class T>
inline T* getPtr(jptr val)
{
    T* ret;
    std::memcpy(&ret, &val, std::min(sizeof(val), sizeof(T*)));
    return ret;
}

template <class T>
inline jlong getJLong(T* val)
{
    jlong ret;
    std::memcpy(&ret, &val, std::min(sizeof(val), sizeof(T*)));
    return ret;
}

#include <unistd.h>

struct fileHandle
{
    FILE* fptr = nullptr;

    fileHandle(FILE* fp) : fptr(fp)
    {}

    ~fileHandle()
    {
        fclose(fptr);
    }
};

inline fileHandle fopen_fd(const char* fname, const char * mode) {
    if (strstr(fname, "/proc/self/fd/") == fname) {
        int fd = atoi(fname + 14);
        if (fd != 0) {
            FILE *fp = fdopen(dup(fd), mode);
            rewind(fp);
            return fp;
        }
    }
    return fopen(fname, mode);
}

#endif //CPPAPP_EXPORT_H
