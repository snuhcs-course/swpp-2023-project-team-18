package snu.swpp.moment.ui.main_writeview.uistate

class AiStoryState(
    val error: Exception?,
    val title: String,
    val content: String,
) {
    companion object {
        @JvmStatic
        fun withError(error: Exception): AiStoryState {
            return AiStoryState(error, "", "")
        }
    }
}
