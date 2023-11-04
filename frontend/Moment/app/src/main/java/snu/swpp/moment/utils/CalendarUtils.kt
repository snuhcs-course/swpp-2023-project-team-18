package snu.swpp.moment.utils

import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import snu.swpp.moment.R
import snu.swpp.moment.data.model.StoryModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters


private val emotionImageList: List<Int> = listOf(
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
        android.R.color.transparent,
)

private val emotionKoreanTextList: List<String> = listOf(
        "설렘", "신남", "기쁨", "행복", "평범", "모름", "슬픔", "우울", "짜증", "화남", "",
)

const val INVALID_EMOTION = 10

fun convertEmotionImage(emotion: Int): Int {
    return emotionImageList[emotion]
}

fun convertEmotionKoreanText(emotion: Int): String {
    return emotionKoreanTextList[emotion]
}

fun isLastWeek(day: CalendarDay): Boolean {
    val date = day.date
    if (day.position == DayPosition.InDate) return false

    if (day.position == DayPosition.OutDate) {
        val firstSat = date.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth())
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
        return date.isAfter(firstSat.plusDays(28))
    }
    if (day.position == DayPosition.MonthDate) {
        val firstSat = date.with(TemporalAdjusters.firstDayOfMonth())
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
        return date.isAfter(firstSat.plusDays(28))

    }
    return false
}

fun fillEmptyStory(storyList: MutableList<StoryModel>, month: YearMonth): List<StoryModel> {
    val result = mutableListOf<StoryModel>()
    var date = LocalDate.of(month.year, month.month, 1);
    val endDate = month.atEndOfMonth();
    var index = 0;
    while (!date.isAfter(endDate)) {
        if (index < storyList.size) {
            val story = storyList[index]
            val createdAt = TimeConverter.convertDateToLocalDate(story.createdAt);
            if (createdAt.isAfter(date)) {
                result.add(StoryModel.empty());
            } else {
                result.add(story)
                index++;
            }
        } else {
            result.add(StoryModel.empty());
        }
        date = date.plusDays(1)
    }
    return result;
}