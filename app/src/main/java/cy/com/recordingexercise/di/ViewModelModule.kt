package cy.com.recordingexercise.di

import cy.com.recordingexercise.ui.viewModel.RecordingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { RecordingViewModel(get()) }
}