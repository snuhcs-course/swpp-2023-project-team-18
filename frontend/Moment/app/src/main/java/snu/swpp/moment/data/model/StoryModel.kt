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
) {
    val createdAt: Date

    init {
        this.createdAt = TimeConverter.convertTimestampToDate(createdAt)
    }

    val emotionInt: Int
        get() {
            return EmotionMap.getEmotionInt(emotion)
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
        )
    }
}