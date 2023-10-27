package snu.swpp.moment.ui.main_writeview.DaySlide

import java.util.Date

class StoryUiState(
    val error: Exception?,
    val isEmpty: Boolean,
    val storyTitle: String,
    val storyContent: String,
    val emotion: Int,
    val tags: List<String>,
    val score: Int,
    val createdAt: Date,
) {
    companion object {
        @JvmStatic
        fun empty() = StoryUiState(
            error = null,
            isEmpty = true,
            storyTitle = "",
            storyContent = "",
            emotion = 0,
            tags = listOf(),
            score = 0,
            createdAt = Date(),
        )
    }
}
