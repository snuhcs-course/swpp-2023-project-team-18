package snu.swpp.moment.ui.main_monthview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import snu.swpp.moment.data.callback.StoryGetCallBack
import snu.swpp.moment.data.callback.TokenCallBack
import snu.swpp.moment.data.model.StoryModel
import snu.swpp.moment.data.repository.AuthenticationRepository
import snu.swpp.moment.data.repository.StoryRepository
import snu.swpp.moment.exception.UnauthorizedAccessException
import snu.swpp.moment.ui.main_writeview.uistate.MonthStoryState
import snu.swpp.moment.utils.TimeConverter
import snu.swpp.moment.utils.fillEmptyStory
import java.lang.Exception
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(
        private val authenticationRepository: AuthenticationRepository,
        private val storyRepository: StoryRepository
) : ViewModel() {
    var currentMonth: MutableLiveData<YearMonth> = MutableLiveData(YearMonth.now())
    var selectedDate: MutableLiveData<LocalDate?> = MutableLiveData(null)
    var calendarDayInfoState: MutableLiveData<CalendarDayInfoState?> =
        MutableLiveData<CalendarDayInfoState?>()
    var calendarDayStates: MutableLiveData<List<CalendarDayState>> =
            MutableLiveData<List<CalendarDayState>>();

    private val monthStoryState = MutableLiveData<MonthStoryState>()

    private val emotionEnumMap: Map<String, Int> = mapOf(
        "excited1" to 0,
        "excited2" to 1,
        "happy1" to 2,
        "happy2" to 3,
        "normal1" to 4,
        "normal2" to 5,
        "sad1" to 6,
        "sad2" to 7,
        "angry1" to 8,
        "angry2" to 9,
        "invalid" to 10
    )

    fun setCurrentMonth(month: YearMonth) {
        currentMonth.value = month
        getStory(month)  // 한달 정보 로드
    }

    fun setSelectedDate(date: LocalDate?) {
        selectedDate.value = date
        if (date == null) {
            calendarDayInfoState.value = null
        } else {
            //calendarDayInfoState.value = getSummaryMock(date)   // 아래쪽에 보여줄 정보 로드
        }
    }

    fun getStory(month: YearMonth) {
        //  api 부르고 MonthStoryState 업데이트
        val startEndTimes = TimeConverter.getOneMonthTimestamps(month);
        authenticationRepository.isTokenValid(object : TokenCallBack {
            override fun onSuccess() {
                val accessToken = authenticationRepository.token.accessToken;
                storyRepository.getStory(accessToken, startEndTimes[0], startEndTimes[1],
                        object: StoryGetCallBack {
                            override fun onSuccess(story: MutableList<StoryModel>) {
                                val convertedList = fillEmptyStory(story, month)
                                monthStoryState.value = MonthStoryState(null, convertedList);
                            }

                            override fun onFailure(error: Exception) {
                                monthStoryState.value = MonthStoryState.withError(error)
                            }

                        })
            }

            override fun onFailure() {
                monthStoryState.value = MonthStoryState.withError(UnauthorizedAccessException())
            }
        })
    }

    fun observeMonthStoryState(observer: Observer<MonthStoryState>) {
        monthStoryState.observeForever(observer);
    }
}