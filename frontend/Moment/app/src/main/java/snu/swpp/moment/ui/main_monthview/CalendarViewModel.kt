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
import snu.swpp.moment.utils.TimeConverter
import java.lang.Exception
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(
    private val authenticationRepository: AuthenticationRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {
    
    private val currentMonth: MutableLiveData<YearMonth> = MutableLiveData(YearMonth.now())
    private val selectedDate: MutableLiveData<LocalDate?> = MutableLiveData(null)

    // 달력 밑에 보여주기 위한 현재 선택된 날짜의 정보
    private val calendarDayInfoState: MutableLiveData<CalendarDayInfoState?> =
        MutableLiveData<CalendarDayInfoState?>()

    // 한 달 동안의 정보
    private val monthStoryState = MutableLiveData<MonthStoryState>()

    fun getCurrentMonth(): YearMonth = currentMonth.value!!

    fun setCurrentMonth(month: YearMonth) {
        currentMonth.value = month
        getStory(month)
    }

    fun getSelectedDate(): LocalDate? = selectedDate.value

    fun setSelectedDate(date: LocalDate?) {
        selectedDate.value = date
        if (date == null) {
            calendarDayInfoState.value = null
        } else {
            calendarDayInfoState.value = getStoryOfDay(date.dayOfMonth)
        }
    }

    fun getStory(month: YearMonth) {
        //  api 부르고 MonthStoryState 업데이트
        val startEndTimes = TimeConverter.getOneMonthTimestamps(month);
        authenticationRepository.isTokenValid(object : TokenCallBack {
            override fun onSuccess() {
                val accessToken = authenticationRepository.token.accessToken;
                storyRepository.getStory(accessToken, startEndTimes[0], startEndTimes[1],
                    object : StoryGetCallBack {
                        override fun onSuccess(storyList: MutableList<StoryModel>) {
                            fillEmptyStory(storyList, month)
                            val datInfoStateList =
                                storyList.map { CalendarDayInfoState.fromStoryModel(it) }
                            monthStoryState.value = MonthStoryState(null, datInfoStateList);
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

    private fun fillEmptyStory(storyList: MutableList<StoryModel>, month: YearMonth) {
        var index = 0;
        var date = LocalDate.of(month.year, month.month, 1);
        val endDate = month.atEndOfMonth();
        while (!date.isAfter(endDate)) {
            val story = storyList[index]
            val createdAt = TimeConverter.convertDateToLocalDate(story.createdAt)
            if (createdAt.isAfter(date)) {
                storyList.add(index, StoryModel.empty())
            }
            index++
            date = date.plusDays(1)
        }
    }

    fun getStoryOfDay(datOfMonth: Int): CalendarDayInfoState {
        return monthStoryState.value!!.storyList[datOfMonth - 1]
    }

    fun observerCalendarDayInfoState(observer: Observer<CalendarDayInfoState?>) {
        calendarDayInfoState.observeForever(observer)
    }
}