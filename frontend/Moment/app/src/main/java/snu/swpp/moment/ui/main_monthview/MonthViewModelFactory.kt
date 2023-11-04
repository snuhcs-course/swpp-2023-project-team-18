package snu.swpp.moment.ui.main_monthview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import snu.swpp.moment.data.repository.AuthenticationRepository
import snu.swpp.moment.data.repository.StoryRepository

class MonthViewModelFactory(
    private val authenticationRepository: AuthenticationRepository,
    private val storyRepository: StoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MonthViewModel::class.java)) {
            return MonthViewModel(authenticationRepository, storyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}