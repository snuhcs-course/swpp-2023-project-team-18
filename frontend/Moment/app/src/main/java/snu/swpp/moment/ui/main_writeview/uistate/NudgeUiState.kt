package snu.swpp.moment.ui.main_writeview.uistate

class NudgeUiState(
    val error: Exception?,
    val isDeleted: Boolean,
    val content: String,
) {
    companion object {
        @JvmStatic
        fun withError(error: Exception) = NudgeUiState(
            error = error,
            isDeleted = false,
            content = "",
        )
    }
}