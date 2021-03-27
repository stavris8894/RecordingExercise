package cy.com.recordingexercise.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.content.FileProvider
import cy.com.recordingexercise.BuildConfig
import cy.com.recordingexercise.R
import cy.com.recordingexercise.databinding.RecordingFragmentBinding
import cy.com.recordingexercise.ui.viewModel.RecordingViewModel
import cy.com.recordingexercise.utils.BindingFragment
import cy.com.recordingexercise.utils.hasPermission
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File


class RecordingFragment :
    BindingFragment<RecordingFragmentBinding>(RecordingFragmentBinding::inflate) {

    private val mRecordingViewModel: RecordingViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewmodel = mRecordingViewModel
        configureObservers()
    }

    private fun configureObservers() {
        mRecordingViewModel.musicPermissionLiveData.observe(viewLifecycleOwner, {
            if (it) {
                checkReadPermissions()
            }
        })
        mRecordingViewModel.recordPermissionsLiveData.observe(viewLifecycleOwner, {
            if (it) {
                checkRecordPermissions()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        mRecordingViewModel.stopRecord()
    }

    override fun onResume() {
        super.onResume()
        mRecordingViewModel.onResume()
    }

    private fun checkReadPermissions() {
        requireActivity().hasPermission(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            R.string.read_permission_rationale
        ) { readOk ->
            if (!readOk)
                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    REQUEST_STORAGE_PERMISSION.toInt()
                )
            else
                openMedia()
        }
    }

    private fun checkRecordPermissions() {
        requireActivity().hasPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            R.string.write_permission_rationale
        ) { writeOk ->
            if (!writeOk)
                showPermissionRequest()
            else {
                requireActivity().hasPermission(
                    Manifest.permission.RECORD_AUDIO,
                    R.string.record_permission_rationale
                ) { recordOk ->
                    if (!recordOk)
                        showPermissionRequest()
                    else {
                        mRecordingViewModel.startRecord()
                    }
                }
            }
        }
    }

    private fun openMedia() {
        val filePath: String = mRecordingViewModel.musicFile
        val uri = FileProvider.getUriForFile(
            requireContext(),
            BuildConfig.APPLICATION_ID + ".fileprovider",
            File(filePath).apply {
                if (exists().not()) return
            }
        )
        val share = Intent(Intent.ACTION_VIEW)
        share.setDataAndType(uri, "audio/wav")
        share.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        requireContext().startActivity(Intent.createChooser(share, "Play Sound File"))
    }

    //show permission request dialog for both write and record
    private fun showPermissionRequest() {
        requestPermissions(
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_RECORD_AND_WRITE_PERMISSION.toInt()
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_RECORD_AND_WRITE_PERMISSION.toInt()) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                mRecordingViewModel.startRecord()
            }
        }
    }


    companion object {
        private const val REQUEST_RECORD_AND_WRITE_PERMISSION: Short = 0x4875
        private const val REQUEST_STORAGE_PERMISSION: Short = 0x4329

    }
}