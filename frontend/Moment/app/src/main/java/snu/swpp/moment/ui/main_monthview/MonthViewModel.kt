package snu.swpp.moment.ui.main_monthview

import android.util.Log
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
import snu.swpp.moment.utils.fillEmptyStory
import java.lang.Exception
import java.time.LocalDate
import java.time.YearMonth

class MonthViewModel(
        private val authenticationRepository: AuthenticationRepository,
        private val storyRepository: StoryRepository
) : ViewModel() {

    private val currentMonth: MutableLiveData<YearMonth> = MutableLiveData(YearMonth.now())
    private val selectedDate: MutableLiveData<LocalDate?> = MutableLiveData(null)

    // 달력 밑에 보여주기 위한 현재 선택된 날짜의 정보
    private val selectedDayState: MutableLiveData<CalendarDayState?> =
            MutableLiveData<CalendarDayState?>()

    // 한 달 동안의 정보
    private val calendarMonthState = MutableLiveData<CalendarMonthState?>()

    fun getCurrentMonth(): YearMonth = currentMonth.value!!

    fun setCurrentMonth(month: YearMonth) {
        currentMonth.value = month
        getStory(month)
    }

    fun getSelectedDate(): LocalDate? = selectedDate.value

    fun setSelectedDate(date: LocalDate?) {
        selectedDate.value = date
        if (date == null) {
            selectedDayState.value = null
        } else {
            selectedDayState.value = getDayState(date.dayOfMonth)
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
                                val calendarDayStateList = storyList.map { CalendarDayState.fromStoryModel(it) }
                                val datInfoStateList = fillEmptyStory(calendarDayStateList, month)
                                calendarMonthState.value = CalendarMonthState(null, datInfoStateList);
                            }

                            override fun onFailure(error: Exception) {
                                calendarMonthState.value = CalendarMonthState.withError(error)
                            }

                        })
            }

            override fun onFailure() {
                calendarMonthState.value =
                        CalendarMonthState.withError(UnauthorizedAccessException())
            }
        })
    }

    fun getDayState(datOfMonth: Int): CalendarDayState? {
        if (calendarMonthState.value == null) {
            Log.d("MonthViewModel", "getDayState: calendarMonthState.value == null")
        }
        return calendarMonthState.value?.storyList?.get(datOfMonth - 1)
    }

    fun observerCalendarMonthState(observer: Observer<CalendarMonthState?>) {
        calendarMonthState.observeForever(observer)
    }

    fun observerSelectedDayState(observer: Observer<CalendarDayState?>) {
        selectedDayState.observeForever(observer)
    }
}