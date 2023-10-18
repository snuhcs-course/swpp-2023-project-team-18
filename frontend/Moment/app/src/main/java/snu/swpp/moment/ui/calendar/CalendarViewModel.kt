package snu.swpp.moment.ui.calendar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel : ViewModel() {
    var currentMonth: MutableLiveData<YearMonth> = MutableLiveData(YearMonth.now())
    var selectedDate: MutableLiveData<LocalDate?> = MutableLiveData(null)

  //  var monthEmotions: MutableLiveData<List<Int>> = MutableLiveData(listOf())
    var daySummaryState: MutableLiveData<DaySummaryState?> = MutableLiveData<DaySummaryState?>()
    var monthState: MutableLiveData<List<CalendarDayState>> = MutableLiveData(listOf())
    val emotionEnumMap: Map<String, Int> = mapOf(
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
        "null" to 10
    )

    fun setCurrentMonth(month: YearMonth) {
        currentMonth.value = month
     //   monthEmotions.value = getMonthEmotionsMock(month)

        monthState.value = getMonthStateMock(month)
    }

    fun setSelectedDate(date: LocalDate?) {
        selectedDate.value = date
        if (date == null) {
            daySummaryState.value = null
        } else {
            daySummaryState.value = getSummaryMock(date)
        }
    }

    // 서버에서 감정 리스트 가져오는 함수
   /* private fun getMonthEmotionsMock(month: YearMonth): List<Int> {
        // API call
        var apiResult: List<String> = List<String>(31, { it -> "normal1" })
        return apiResult.map { emotionEnumMap[it]!! }
    }*/
    private fun getMonthStateMock(month:YearMonth):List<CalendarDayState>{
      return  List<CalendarDayState>(31){
            CalendarDayState(emotionEnumMap.values.random(),it % 7 == 0)
        }

    }

    // 서버에서 summary 받아오는 함수
    private fun getSummaryMock(date: LocalDate): DaySummaryState {
        // API call
        return DaySummaryState(
            date,
            "title",
            "story",
            "normal1",
            listOf("tag1", "tag2"),
            0
        )
    }
}