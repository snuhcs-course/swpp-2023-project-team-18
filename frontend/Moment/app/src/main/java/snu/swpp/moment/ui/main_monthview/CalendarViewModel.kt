package snu.swpp.moment.ui.main_monthview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import snu.swpp.moment.ui.main_writeview.uistate.MonthStoryState
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel : ViewModel() {
    var currentMonth: MutableLiveData<YearMonth> = MutableLiveData(YearMonth.now())
    var selectedDate: MutableLiveData<LocalDate?> = MutableLiveData(null)
    var calendarDayInfoState: MutableLiveData<CalendarDayInfoState?> =
        MutableLiveData<CalendarDayInfoState?>()
    var calendarDayStates: MutableLiveData<List<CalendarDayState>> = MutableLiveData(listOf())

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
        calendarDayStates.value = getDayStatesMock(month)   // 한달 정보 로드
    }

    fun setSelectedDate(date: LocalDate?) {
        selectedDate.value = date
        if (date == null) {
            calendarDayInfoState.value = null
        } else {
            calendarDayInfoState.value = getSummaryMock(date)   // 아래쪽에 보여줄 정보 로드
        }
    }

    fun getStory(month: YearMonth) {
        // TODO
        //  api 부르고 MonthStoryState 업데이트
    }

    fun observeMonthStoryState(observer: Observer<MonthStoryState>) {
        // TODO:
    }

    // FIXME ("Deprecated")
    // 서버에서 감정 리스트 가져오는 함수
    private fun getDayStatesMock(month: YearMonth): List<CalendarDayState> {
        // NOTE: List 길이가 31이 아니면 IndexOutOfBound 에러가 남 (원인 불명)
        val apiResult = List<CalendarDayState>(31) {
            CalendarDayState(emotionEnumMap.values.random(), it % 7 == 0)
        }
        return apiResult
    }

    // 서버에서 summary 가져오는 함수
    private fun getSummaryMock(date: LocalDate): CalendarDayInfoState {
        return CalendarDayInfoState(
            date = date,
            storyTitle = String.format("title %d", date.dayOfMonth),
            storyContent = "story",
            emotion = calendarDayStates.value!![date.dayOfMonth - 1].emotion,
            tags = listOf("tag1", "tag2"),
            score = 0,
            isAutoCompleted = calendarDayStates.value!![date.dayOfMonth - 1].isAutoCompleted,
        )
    }
}