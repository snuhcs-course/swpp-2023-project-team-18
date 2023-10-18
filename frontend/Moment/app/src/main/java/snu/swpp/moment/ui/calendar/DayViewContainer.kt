package snu.swpp.moment.ui.calendar

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
    val divider:View = view.findViewById(R.id.divider)
    val completionView:View = view.findViewById(R.id.completionView)
    fun initialize(calendarView: CalendarView, viewModel: CalendarViewModel, day: CalendarDay) {
        // 날짜 선택 시 동작
        view.setOnClickListener {
            if (day.position != DayPosition.MonthDate) {
                return@setOnClickListener
            }

            val currentSelection = viewModel.selectedDate.value
            if (currentSelection == day.date) {
                // 날짜 선택 해제 후 reload
                // FIXME: 선택 해제 뺄 거면 이 부분 뺴기
                viewModel.setSelectedDate(null)
                calendarView.notifyDateChanged(currentSelection)
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