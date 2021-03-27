package cy.com.recordingexercise.utils

import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import cy.com.recordingexercise.R
import org.jetbrains.anko.alert

/**
 * checks if the app has been granted the permission or if rationale should be displayed
 * @param permission the permission to check
 * @param rationale the rationale message to show in case needed
 * @param callback returns if permission is granted
 */
fun FragmentActivity.hasPermission(
    permission: String,
    rationale: Int,
    callback: (hasPermission: Boolean) -> Unit
) {
    if (shouldShowRequestPermissionRationale(permission)) {
        alert(rationale) {
            positiveButton(R.string.ok) { callback(false) }
            show()
        }
    } else {
        ActivityCompat.checkSelfPermission(this, permission).let { status ->
            when (status) {
                PackageManager.PERMISSION_GRANTED -> callback(true)
                else -> callback(false)
            }
        }
    }
}