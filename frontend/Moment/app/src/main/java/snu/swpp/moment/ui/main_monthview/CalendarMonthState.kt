package snu.swpp.moment.ui.main_monthview

class CalendarMonthState(
    val error: Exception?,
    val storyList: List<CalendarDayState>
) {
    companion object {
        @JvmStatic
        fun withError(error: Exception) = CalendarMonthState(
            error = error,
            storyList = listOf()
        )
    }
}