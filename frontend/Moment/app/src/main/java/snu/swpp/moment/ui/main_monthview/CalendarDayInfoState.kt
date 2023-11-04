package snu.swpp.moment.ui.main_monthview

import snu.swpp.moment.utils.convertEmotionImage
import snu.swpp.moment.utils.convertEmotionKoreanText
import java.time.LocalDate

class CalendarDayInfoState(
    val date: LocalDate,
    val storyTitle: String,
    val storyContent: String,
    val emotion: Int,
    val tags: List<String>,
    val score: Int = 0,
    val isAutoCompleted: Boolean = false,
) {
    val emotionImage: Int = convertEmotionImage(emotion)
    val emotionKoreanText: String = convertEmotionKoreanText(emotion)
}