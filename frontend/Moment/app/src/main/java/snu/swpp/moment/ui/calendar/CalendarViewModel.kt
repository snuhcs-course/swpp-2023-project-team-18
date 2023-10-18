package snu.swpp.moment.ui.calendar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import snu.swpp.moment.R
import java.time.LocalDate
import java.time.YearMonth
import java.util.Random

class CalendarViewModel : ViewModel() {
    var currentMonth: MutableLiveData<YearMonth> = MutableLiveData(YearMonth.now())
    var selectedDate: MutableLiveData<LocalDate?> = MutableLiveData(null)

    private var monthEmotionStrings: List<String> = listOf()
    var monthEmotionImages: MutableLiveData<List<Int>> = MutableLiveData(listOf())
    var monthAutoFinished: MutableLiveData<List<Boolean>> = MutableLiveData(listOf())
    var daySummaryState: MutableLiveData<DaySummaryState?> = MutableLiveData<DaySummaryState?>()

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
    )

    private val emotionImageMap: List<Int> = listOf(
        R.drawable.icon_sunny,
        R.drawable.icon_sunny,
        R.drawable.icon_sun_cloud,
        R.drawable.icon_sun_cloud,
        R.drawable.icon_cloud,
        R.drawable.icon_cloud,
        R.drawable.icon_rain,
        R.drawable.icon_rain,
        R.drawable.icon_lightning,
        R.drawable.icon_lightning,
    )

    fun setCurrentMonth(month: YearMonth) {
        currentMonth.value = month
        monthEmotionStrings = getMonthEmotionStringsMock()
        monthEmotionImages.value = monthEmotionStrings.map { emotionStringToImage(it) }
        monthAutoFinished.value = getMonthAutoFinishedMock(month)
    }

    fun setSelectedDate(date: LocalDate?) {
        selectedDate.value = date
        if (date == null) {
            daySummaryState.value = null
        } else {
            daySummaryState.value = getSummaryMock(date)
        }
    }

    fun emotionStringToImage(imageString: String): Int {
        return if (emotionEnumMap.containsKey(imageString)) {
            emotionImageMap[emotionEnumMap[imageString]!!]
        } else {
            -1
        }
    }

    // 서버에서 감정 리스트 가져오는 함수
    private fun getMonthEmotionStringsMock(): List<String> {
        val emotions: List<String> = listOf(
            "excited1",
            "excited2",
            "happy1",
            "happy2",
            "normal1",
            "normal2",
            "sad1",
            "sad2",
            "angry1",
            "angry2"
        )
        val random = Random()
        return List<String>(31) { it ->
            val emotionEnum = random.nextInt(emotions.size)
            emotions[emotionEnum]
        }
    }

    // 서버에서 자동마무리 여부 가져오는 함수
    private fun getMonthAutoFinishedMock(month: YearMonth): List<Boolean> {
        // API call
        val apiResult: List<Boolean> = List<Boolean>(31) { it -> true }
        return apiResult
    }

    // 서버에서 summary 가져오는 함수
    private fun getSummaryMock(date: LocalDate): DaySummaryState {
        // API call
        return DaySummaryState(
            date = date,
            storyTitle = String.format("title %d", date.dayOfMonth),
            storyContent = "story",
            emotion = monthEmotionStrings[date.dayOfMonth - 1],
            tags = listOf("tag1", "tag2"),
            score = 0,
        )
    }
}