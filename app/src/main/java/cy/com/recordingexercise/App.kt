package cy.com.recordingexercise

import android.app.Application
import androidx.annotation.StringRes
import cy.com.recordingexercise.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.*

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        startKoin {
            androidContext(this@App)
            modules(listOf(viewModelModule))
        }

    }

    companion object {
        private lateinit var instance: App

        fun getString(@StringRes resId: Int): String {
            return instance.resources.getString(resId)
        }

        fun getString(@StringRes resId: Int, arg: String): String {
            return instance.resources.getString(resId, arg)
        }
    }
}