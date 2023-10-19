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
            if (day.position != DayPosition.MonthDate) {
                return@setOnClickListener
            } else if (viewModel.calendarDayStates.value!![day.date.dayOfMonth - 1].emotion == NULL_EMOTION) {
                return@setOnClickListener
            }

            val currentSelection = viewModel.selectedDate.value
            if (currentSelection == day.date) {
                // 한 번 더 눌러도 선택 해제 안 됨
//                viewModel.setSelectedDate(null)
//                calendarView.notifyDateChanged(currentSelection)
            } else {
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