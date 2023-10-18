package snu.swpp.moment.ui.calendar

import java.time.LocalDate

class CalendarDayInfoState(
    val date: LocalDate,
    val storyTitle: String,
    val storyContent: String,
    val emotion: Int,
    val tags: List<String>,
    val score: Int = 0,
) {
    val emotionImage: Int = convertEmotionImage(emotion)
    val emotionKoreanText: String = convertEmotionKoreanText(emotion)
}