package snu.swpp.moment.ui.main_writeview.uistate

import snu.swpp.moment.utils.EmotionMap
import java.util.Date

class StoryUiState(
    val error: Exception?,  // 성공 시 null
    val isEmpty: Boolean,   // 저장된 story가 없으면 true
    val title: String,
    val content: String,
    val emotion: Int,
    val tags: List<String>,
    val score: Int,
    val createdAt: Date,
    val isPointCompleted: Boolean
) {
    companion object {
        @JvmStatic
        fun empty() = StoryUiState(
            error = null,
            isEmpty = true,
            title = "",
            content = "",
            emotion = 0,
            tags = listOf(),
            score = 0,
            createdAt = Date(),
            isPointCompleted = false
        )

        @JvmStatic
        fun withError(error: Exception) = StoryUiState(
            error = error,
            isEmpty = true,
            title = "",
            content = "",
            emotion = 0,
            tags = listOf(),
            score = 0,
            createdAt = Date(),
            isPointCompleted = false
        )
    }

    fun isEmotionInvalid() = emotion == EmotionMap.INVALID_EMOTION
}
