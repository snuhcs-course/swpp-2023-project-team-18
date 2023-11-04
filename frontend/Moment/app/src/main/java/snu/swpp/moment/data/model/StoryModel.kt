package snu.swpp.moment.data.model

import snu.swpp.moment.utils.EmotionMap
import snu.swpp.moment.utils.TimeConverter
import java.util.Date

class StoryModel(
    val id: Int,
    val emotion: String,
    val score: Int,
    val title: String,
    val content: String,
    val hashtags: List<HashtagModel> = listOf(),
    createdAt: Long?,
    val isPointCompleted: Boolean,
    val isEmpty: Boolean = false,
) {
    val createdAt: Date

    init {
        this.createdAt = TimeConverter.convertTimestampToDate(createdAt)
    }

    val emotionInt: Int
        get() {
            return EmotionMap.getEmotionInt(emotion)
        }

    val isEmotionInvalid: Boolean
        get() {
            return emotionInt == EmotionMap.INVALID_EMOTION
        }

    companion object {
        @JvmStatic
        fun empty(): StoryModel = StoryModel(
            -1,
            "invalid",
            3,
            "",
            "",
            listOf(),
            0L,
            false,
            isEmpty = true,
        )
    }
}