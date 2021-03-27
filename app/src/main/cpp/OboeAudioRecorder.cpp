//
// Created by Stavros Georgiou on 3/23/21.
//
#include <jni.h>
#include <string>
#include "oboe/include/oboe/Oboe.h"
#include "OboeAudioRecorder.h"
#include "oboe/samples/debug-utils/logging_macros.h"
#include <fstream>

namespace little_endian_io {
    template<typename Word>
    std::ostream &write_word(std::ostream &outs, Word value, unsigned size = sizeof(Word)) {
        for (; size; --size, value >>= 8)
            outs.put(static_cast <char> (value & 0xFF));
        return outs;
    }
}
using namespace little_endian_io;

class OboeAudioRecorder : public oboe::AudioStreamCallback {

private:
    oboe::ManagedStream outStream;
    oboe::AudioStream *stream{};

    oboe::DataCallbackResult
    onAudioReady(oboe::AudioStream *oboeStream, void *audioData, int32_t numFrames) override {
        LOGE("onAudioReady");
    }

    static OboeAudioRecorder *singleton;

    explicit OboeAudioRecorder() = default;

public:
    static OboeAudioRecorder *get() {
        if (singleton == nullptr)
            singleton = new OboeAudioRecorder();
        return singleton;
    }

    bool isRecording = true;

    void StopAudioRecorder() {
        this->isRecording = false;
    }

    void
    StartAudioRecorder(const char *fullPathToFile, const int recordingFreq,
                       const int sessionId) {
        this->isRecording = true;
        oboe::AudioStreamBuilder builder;
        builder.setDirection(oboe::Direction::Input);
        builder.setPerformanceMode(oboe::PerformanceMode::LowLatency);
        builder.setFormat(oboe::AudioFormat::I16);
        builder.setChannelCount(oboe::ChannelCount::Mono);
        builder.setInputPreset(oboe::InputPreset::Unprocessed);
        builder.setSharingMode(oboe::SharingMode::Shared);
        builder.setSampleRate(recordingFreq);
        builder.setAudioApi(oboe::AudioApi::Unspecified);

        // Wave file generating stuff (from https://www.cplusplus.com/forum/beginner/166954/)
        int bitsPerSample = 16; // multiple of 8
        int numChannels = 1; // 2 for stereo, 1 for mono

        std::ofstream ofstream;
        const char *path = fullPathToFile;
        ofstream.open(path, std::ios::binary);
        // Write the file headers
        ofstream << "RIFF----WAVEfmt ";     // (chunk size to be filled in later)
        write_word(ofstream, 16, 4);  // no extension data
        write_word(ofstream, 1, 2);  // PCM - integer samples
        write_word(ofstream, numChannels, 2);  // one channel (mono) or two channels (stereo file)
        write_word(ofstream, recordingFreq, 4);  // samples per second (Hz)
        // (Sample Rate * BitsPerSample * Channels) / 8
        write_word(ofstream, (recordingFreq * bitsPerSample * numChannels) / 8, 4);
        // data block size (size of two integer samples, one for each channel, in bytes)
        write_word(ofstream, 4, 2);
        write_word(ofstream, bitsPerSample,
                   2);  // number of bits per sample (use streamState multiple of 8)

        // Write the data chunk header
        size_t data_chunk_pos = ofstream.tellp();
        ofstream << "data----";  // (chunk size to be filled in later)

        oboe::Result r = builder.openStream(&stream);
        if (r != oboe::Result::OK) {
            return;
        }

        r = stream->requestStart();
        if (r != oboe::Result::OK) {
            return;
        }

        auto streamState = stream->getState();
        if (streamState == oboe::StreamState::Started) {

            constexpr int kMillisecondsToRecord = 2;
            auto requestedFrames = (int32_t) (kMillisecondsToRecord *
                                              (stream->getSampleRate() / oboe::kMillisPerSecond));

            int16_t mybuffer[requestedFrames];
            constexpr int64_t kTimeoutValue = 3 * oboe::kNanosPerMillisecond;

            int framesRead;
            do {
                auto result = stream->read(mybuffer, requestedFrames, 0);
                if (result != oboe::Result::OK) {
                    break;
                }
                framesRead = result.value();
                if (framesRead > 0) {
                    break;
                }
            } while (framesRead != 0);

            while (isRecording) {
                auto result = stream->read(mybuffer, requestedFrames, kTimeoutValue * 1000);
                if (result == oboe::Result::OK) {
                    auto value = result.value();
                    int N = value / 16;
                    for (int i = N + 1; i < value; i++) {
                        write_word(ofstream, (int) (mybuffer[i] + 0.5 * mybuffer[i - N]),
                                   2);
                    }
                } else {
                    auto error = convertToText(result.error());
                    __android_log_print(ANDROID_LOG_INFO, "OboeAudioRecorder", "error = %s", error);
                }
            }

            stream->requestStop();
            stream->close();

            // (We'll need the final file size to fix the chunk sizes above)
            size_t file_length = ofstream.tellp();

            // Fix the data chunk header to contain the data size
            ofstream.seekp(data_chunk_pos + 4);
            write_word(ofstream, file_length - data_chunk_pos + 8);

            // Fix the file header to contain the proper RIFF chunk size, which is (file size - 8) bytes
            ofstream.seekp(0 + 4);
            write_word(ofstream, file_length - 8, 4);
            ofstream.close();
        }
    }

};

OboeAudioRecorder *OboeAudioRecorder::singleton = nullptr;

