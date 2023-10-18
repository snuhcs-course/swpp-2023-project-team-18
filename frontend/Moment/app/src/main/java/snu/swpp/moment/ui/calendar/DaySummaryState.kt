package snu.swpp.moment.ui.calendar

import java.time.LocalDate

class DaySummaryState(
    val date: LocalDate,
    val storyTitle: String,
    val storyContent: String,
    val emotion: String,
    val tags: List<String>,
    val score: Int = 0,
)