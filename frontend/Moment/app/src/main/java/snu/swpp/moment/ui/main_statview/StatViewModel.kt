package snu.swpp.moment.ui.main_statview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import snu.swpp.moment.data.callback.StoryGetCallBack
import snu.swpp.moment.data.callback.TokenCallBack
import snu.swpp.moment.data.model.StoryModel
import snu.swpp.moment.data.repository.AuthenticationRepository
import snu.swpp.moment.data.repository.StoryRepository
import snu.swpp.moment.exception.UnauthorizedAccessException
import snu.swpp.moment.ui.main_monthview.CalendarDayState
import snu.swpp.moment.ui.main_monthview.CalendarMonthState
import snu.swpp.moment.utils.TimeConverter
import snu.swpp.moment.utils.fillEmptyStory
import java.time.LocalDate

class StatViewModel(
    private val authenticationRepository: AuthenticationRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {
    val today: MutableLiveData<LocalDate> = MutableLiveData(LocalDate.now())
    val stat: MutableLiveData<StatState> = MutableLiveData()
    fun getStats(isMonth: Boolean) {
        val todayDate = TimeConverter.getToday()
        today.value = todayDate
        val startEndTimes =
            if (isMonth) TimeConverter.getRecentMonthTimestamps(todayDate) else TimeConverter.getRecentWeekTimestamps(
                todayDate
            )
        authenticationRepository.isTokenValid(object : TokenCallBack {
            override fun onSuccess() {
                val accessToken = authenticationRepository.token.accessToken;
                storyRepository.getStory(accessToken, startEndTimes[0], startEndTimes[1],
                    object : StoryGetCallBack {
                        override fun onSuccess(storyList: MutableList<StoryModel>) {
                            stat.value = StatState.fromStoryModels(storyList, todayDate)
                        }

                        override fun onFailure(error: Exception) {
                            stat.value = StatState.withError(error)
                        }

                    })
            }

            override fun onFailure() {
                stat.value = StatState.withError(UnauthorizedAccessException())

            }

        })
    }


}