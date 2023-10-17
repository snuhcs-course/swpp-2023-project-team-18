package snu.swpp.moment.ui.calendar

import java.time.LocalDate

class DaySummaryState(
    date: LocalDate, title: String,
    story: String, emotion: String,
    tags: List<String>, score: Int = 0
) {
    val date: LocalDate = date
    val title: String = title
    val story: String = story
    val emotion: String = emotion
    val tags: List<String> = tags
    val score: Int = score

}