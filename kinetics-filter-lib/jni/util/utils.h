#include <android/log.h>

//logging helper MACROs
#define LOG_D(format, ...) __android_log_print(ANDROID_LOG_DEBUG, __FILE__, format, ##__VA_ARGS__);
//#define LOG_D(message) __android_log_write(ANDROID_LOG_DEBUG, __FILE__, param);

#define LOG_I(format, ...) __android_log_print(ANDROID_LOG_INFO, __FILE__, format, ##__VA_ARGS__);
//#define LOG_I(message) __android_log_write(ANDROID_LOG_INFO, __FILE__, param);

#define LOG_W(format, ...) __android_log_print(ANDROID_LOG_WARN, __FILE__, format, ##__VA_ARGS__);
//#define LOG_W(message) __android_log_write(ANDROID_LOG_WARN, __FILE__, param);

#define LOG_E(format, ...) __android_log_print(ANDROID_LOG_ERROR, __FILE__, format, ##__VA_ARGS__);

static const char* JNI_INT_FIELD = "I";

