package cy.com.recordingexercise.utils

import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.FX_KEY_CLICK
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.os.Environment
import java.io.File


class RecordHelper {

    fun start(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.loadSoundEffects()
        Thread {
            startRecording(
                getOutputDirectory().toString() + "/record.wav",
                48000,
                audioManager.generateAudioSessionId()
            )
        }.start()
    }

    fun stopRecord() {
        Thread { stopRecording() }.start()
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

    private external fun startRecording(
        fullPathToFile: String,
        recordingFrequency: Int,
        audioSessionId: Int
    ): Boolean

    private external fun stopRecording(): Boolean

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }

        fun getOutputDirectory(): File {
            val folder =
                File(Environment.getExternalStorageDirectory().path.toString() + "/Recorders")
            if (folder.exists()) {
                if (folder.isDirectory) {
                    // The Recorders directory exists
                } else {
                    // Create the Recorders directory
                    folder.mkdir()
                }
            } else {
                // Create the Recorders directory
                folder.mkdir()
            }
            return folder
        }

    }
}