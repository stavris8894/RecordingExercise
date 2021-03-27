package cy.com.recordingexercise.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import cy.com.recordingexercise.R

fun FragmentActivity.getCurrentFragment(id: Int = R.id.fragmentContainer): Fragment? {
    return supportFragmentManager.findFragmentById(id)
}

fun FragmentActivity.replaceFragment(fragment: Fragment, id: Int = R.id.fragmentContainer) {
    supportFragmentManager.commit {
        setReorderingAllowed(true)
        replace(id, fragment)
    }
}
