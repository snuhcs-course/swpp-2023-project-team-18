package snu.swpp.moment.ui.main_statview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import snu.swpp.moment.data.repository.AuthenticationRepository
import snu.swpp.moment.data.repository.StoryRepository
import snu.swpp.moment.ui.main_monthview.MonthViewModel

class StatViewModelFactory(
    private val authenticationRepository: AuthenticationRepository,
    private val storyRepository: StoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatViewModel::class.java)) {
            return StatViewModel(authenticationRepository, storyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}