package cy.com.recordingexercise.id

import cy.com.recordingexercise.ui.viewModel.RecordingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { RecordingViewModel(get()) }
}