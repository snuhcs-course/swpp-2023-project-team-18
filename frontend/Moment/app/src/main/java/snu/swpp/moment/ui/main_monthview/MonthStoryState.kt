package snu.swpp.moment.ui.main_monthview

class MonthStoryState(
    val error: Exception?,
    val storyList: List<CalendarDayInfoState>
) {
    companion object {
        @JvmStatic
        fun withError(error: Exception) = MonthStoryState(
            error = error,
            storyList = listOf()
        )
    }
}