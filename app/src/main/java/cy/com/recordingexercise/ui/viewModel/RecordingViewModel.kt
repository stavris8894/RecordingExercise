package cy.com.recordingexercise.ui.viewModel

import android.app.Application
import android.view.View
import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cy.com.recordingexercise.utils.ObservableViewModel
import cy.com.recordingexercise.utils.RecordHelper
import java.io.File

class RecordingViewModel(application: Application) : ObservableViewModel(application) {

    private val recordHelper = RecordHelper()

    val musicFile = RecordHelper.getOutputDirectory().path + "/record.wav"

    private val _checkMusicPermissions = MutableLiveData<Boolean>()
    val musicPermissionLiveData: LiveData<Boolean> = _checkMusicPermissions

    private val _checkRecordPermissions = MutableLiveData<Boolean>()
    val recordPermissionsLiveData: LiveData<Boolean> = _checkRecordPermissions

    fun startRecord() {
        recordHelper.start(getApplication())
        isRecording = true
    }

    fun stopRecord() {
        recordHelper.stopRecord()
        isRecording = false
    }

    fun onResume() {
        notifyChange()
    }

    var isRecording: Boolean = false
        set(value) {
            field = value
            notifyChange()
        }

    fun onRecordButtonClick(view: View) {
        if (isRecording) {
            //STOP RECORD
            stopRecord()
        } else {
            //START RECORD
            _checkRecordPermissions.value = true
        }
    }

    fun onMusicButtonClick(view: View) {
        _checkMusicPermissions.value = true
    }

    @get:Bindable
    val musicButtonEnabled: Boolean
        get() {
            return File(musicFile).exists()
        }

    @get:Bindable
    val username: String
        get() = when (isRecording) {
            true -> "Stop Recording"
            false -> "Start Recording"
        }

    companion object {
        private val TAG = RecordingViewModel::class.java.simpleName

    }

}