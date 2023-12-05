package snu.swpp.moment.ui.main_writeview.uistate

class CompletionState(
    val error: Exception?,
    val storyId: Int,
) {
    companion object {
        @JvmStatic
        fun withError(error: Exception): CompletionState {
            return CompletionState(error, -1)
        }
    }
}