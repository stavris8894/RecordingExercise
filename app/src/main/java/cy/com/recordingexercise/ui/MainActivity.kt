package cy.com.recordingexercise.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cy.com.recordingexercise.R
import cy.com.recordingexercise.utils.replaceFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        replaceFragment(RecordingFragment())
    }
}