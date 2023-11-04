package snu.swpp.moment.ui.main_monthview

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.ViewContainer
import snu.swpp.moment.R


// 각 날짜 칸
class DayViewContainer(view: View) : ViewContainer(view) {
    val textView: TextView = view.findViewById(R.id.calendarDayText)
    val imageView: ImageView = view.findViewById(R.id.calendarDayImage)
    val divider: View = view.findViewById(R.id.divider)
    val autoCompletedDot: View = view.findViewById(R.id.autoCompletedDot)

    fun initialize(calendarView: CalendarView, viewModel: CalendarViewModel, day: CalendarDay) {
        // 날짜 선택 시 동작
        view.setOnClickListener {
            // 한 달의 날짜 범위를 벗어나거나 스토리가 없는 날은 클릭할 수 없음
            if (day.position != DayPosition.MonthDate) {
                return@setOnClickListener
            } else if (viewModel.getStoryOfDay(day.date.dayOfMonth).isEmotionInvalid) {
                return@setOnClickListener
            }

            val currentSelection = viewModel.getSelectedDate()
            if (currentSelection != day.date) {
                // 날짜 선택
                viewModel.setSelectedDate(day.date)
                calendarView.notifyDateChanged(day.date)
                if (currentSelection != null) {
                    calendarView.notifyDateChanged(currentSelection)
                }
            }
        }
    }
}