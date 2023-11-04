package snu.swpp.moment.ui.main_monthview

import snu.swpp.moment.data.model.StoryModel
import snu.swpp.moment.utils.EmotionMap
import snu.swpp.moment.utils.TimeConverter
import snu.swpp.moment.utils.convertEmotionImage
import snu.swpp.moment.utils.convertEmotionKoreanText
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

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

    companion object {
        @JvmStatic
        fun fromStoryModel(model: StoryModel): CalendarDayInfoState {
            return CalendarDayInfoState(
                date = TimeConverter.convertDateToLocalDate(model.createdAt),
                storyTitle = model.title,
                storyContent = model.content,
                emotion = model.emotionInt,
                tags = model.hashtags.map { it.content },
                score = model.score,
                isAutoCompleted = !model.isPointCompleted,
            )
        }
    }

    val dateText = "%d. %d. %d. %s".format(
        date.year,
        date.monthValue,
        date.dayOfMonth,
        date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN),
    )

    val isEmotionInvalid: Boolean
        get() {
            return emotion == EmotionMap.INVALID_EMOTION
        }
}