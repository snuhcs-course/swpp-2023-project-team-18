package snu.swpp.moment.ui.main_writeview.uistate

import snu.swpp.moment.data.model.StoryModel
import java.lang.Error

class MonthStoryState(
        val error: Exception?,
        val storyList: List<StoryModel>
) {
    companion object {
        @JvmStatic
        fun withError(error: Exception) = MonthStoryState(
                error = error,
                storyList = listOf()
        )
    }
}