package snu.swpp.moment.ui.main_monthview

import snu.swpp.moment.utils.convertEmotionImage

class CalendarDayState(val emotion: Int, val isAutoCompleted: Boolean) {
    val emotionImage: Int = convertEmotionImage(emotion)
}
