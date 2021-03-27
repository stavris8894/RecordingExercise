#include <jni.h>
#include <string>
#include <android/log.h>
#include <oboe/Oboe.h>
#include <thread>
#include "OboeAudioRecorder.h"
#include "OboeAudioRecorder.cpp"

extern "C" JNIEXPORT jboolean JNICALL
Java_cy_com_recordingexercise_utils_RecordHelper_startRecording(
        JNIEnv *env,
        jobject MainActivity,
        jstring fullPathToFile,
        jint recordingFrequency, jint audio_session_id) {

    const char *path = (*env).GetStringUTFChars(fullPathToFile, 0);
    const int freq = (int) recordingFrequency;

    static auto a = OboeAudioRecorder::get();
    a->StartAudioRecorder(path, freq, audio_session_id);
    return true;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_cy_com_recordingexercise_utils_RecordHelper_stopRecording(
        JNIEnv *env,
        jobject MainActivity
) {
    static auto a = OboeAudioRecorder::get();
    a->StopAudioRecorder();
    return true;
}

