package snu.swpp.moment.ui.main_writeview.DaySlide

import java.lang.Exception

class StoryUiState(
    val error: Exception?,
    val isEmpty: Boolean,
    val storyTitle: String,
    val storyContent: String,
    val emotion: Int,
    val tags: List<String>,
    val score: Int,
) {
}
