package snu.swpp.moment.ui.calendar

class CalendarDayState(val emotion: Int, val isAutoCompleted: Boolean) {
    val emotionImage: Int = convertEmotionImage(emotion)
}
