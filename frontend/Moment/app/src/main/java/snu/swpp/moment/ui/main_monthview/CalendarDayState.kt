package snu.swpp.moment.ui.main_monthview

class CalendarDayState(val emotion: Int, val isAutoCompleted: Boolean) {
    val emotionImage: Int = convertEmotionImage(emotion)
}
