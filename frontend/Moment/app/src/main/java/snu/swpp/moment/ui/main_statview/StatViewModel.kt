package snu.swpp.moment.ui.main_statview

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
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
    private val emptyMap: Map<String, Int> = mapOf()
    val hashtags = mutableStateOf(emptyMap)
    // 기간을 표시하기 위한 변수
    val startDate: MutableLiveData<LocalDate> = MutableLiveData()
    val endDate: MutableLiveData<LocalDate> = MutableLiveData(LocalDate.now())

    // 버튼 타입을 나타내는 livedata
    val selectedButtonType:MutableLiveData<ButtonType> = MutableLiveData(ButtonType.WEEK)


    fun getStats(isMonth: Boolean) {
        val todayDate = TimeConverter.getToday()
        today.value = todayDate
        val startEndTimes =
            if (isMonth) TimeConverter.getRecentMonthTimestamps(todayDate) else TimeConverter.getRecentWeekTimestamps(
                todayDate
            )

        // 기간 표시를 위한 로직
        val periodStartDate = if (isMonth) {
            todayDate.minusDays(30)
        } else {
            todayDate.minusDays(7)
        }
        startDate.value = periodStartDate
        endDate.value = todayDate

        authenticationRepository.isTokenValid(object : TokenCallBack {
            override fun onSuccess() {
                val accessToken = authenticationRepository.token.accessToken;
                storyRepository.getStory(accessToken, startEndTimes[0], startEndTimes[1],
                    object : StoryGetCallBack {
                        override fun onSuccess(storyList: MutableList<StoryModel>) {
                            stat.value = StatState.fromStoryModels(storyList, todayDate)
                            hashtags.value = StatState.storiesToHashtags(storyList)
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

        // 버튼의 상태 업데이트,
        selectedButtonType.value = if (isMonth) ButtonType.MONTH else ButtonType.WEEK

    }

    enum class ButtonType {
        WEEK, MONTH
    }
}