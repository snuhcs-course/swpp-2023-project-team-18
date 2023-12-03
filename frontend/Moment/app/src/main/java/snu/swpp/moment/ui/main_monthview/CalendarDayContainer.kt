package snu.swpp.moment.ui.main_monthview

import android.graphics.Typeface
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.ViewContainer
import snu.swpp.moment.R


// 각 날짜 칸
class CalendarDayContainer(view: View) : ViewContainer(view) {
    val textView: TextView = view.findViewById(R.id.calendarDayText)
    val imageView: ImageView = view.findViewById(R.id.calendarDayImage)
    val divider: View = view.findViewById(R.id.divider)
    val autoCompletedDot: View = view.findViewById(R.id.autoCompletedDot)

    fun initialize(calendarView: CalendarView, viewModel: MonthViewModel, day: CalendarDay) {
        // 날짜 선택 시 동작
        view.setOnClickListener {
            // 한 달의 날짜 범위를 벗어나거나 스토리가 없는 날은 클릭할 수 없음
            if (day.position != DayPosition.MonthDate) {
                return@setOnClickListener
            }
            val calendarDayState = viewModel.getDayState(day.date.dayOfMonth)
            if (calendarDayState == null || calendarDayState.isStoryInvalid) {
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

    fun updateDateText(day: Int, selected: Boolean) {
        textView.text = day.toString()

        if (selected) {
            // 선택된 날짜: Red + Bold
            textView.setTextColor(ContextCompat.getColor(view.context!!, R.color.red))
            val typeface: Typeface =
                ResourcesCompat.getFont(view.context!!, R.font.maruburi_bold)!!
            textView.typeface = typeface
        } else {
            textView.setTextColor(
                ContextCompat.getColor(view.context!!, R.color.black)
            )
            val typeface: Typeface =
                ResourcesCompat.getFont(view.context!!, R.font.maruburi_regular)!!
            textView.typeface = typeface
        }
    }

    fun setUiMonthDate(calendarDayState: CalendarDayState) {
        if (calendarDayState.isStoryInvalid) {
            imageView.visibility = View.INVISIBLE
            autoCompletedDot.visibility = View.GONE
        } else {
            imageView.setImageResource(calendarDayState.emotionImage)
            imageView.visibility = View.VISIBLE
            autoCompletedDot.visibility =
                if (calendarDayState.isAutoCompleted) View.VISIBLE else View.GONE
        }
    }

    fun setUiOutDate() {
        // 이전/다음 달의 날짜는 회색으로 표시 & 이미지 숨김
        textView.setTextColor(ContextCompat.getColor(view.context!!, R.color.gray))
        imageView.visibility = View.INVISIBLE
        autoCompletedDot.visibility = View.GONE
    }

    fun setUiScrolling() {
        // 스크롤 도중 이전/다음 달은 내용 안 보여줌
        imageView.setImageResource(android.R.color.transparent)
        autoCompletedDot.visibility = View.GONE
    }

    fun setDividerLineVisible(visible: Boolean) {
        if (visible) {
            divider.visibility = View.VISIBLE
        } else {
            divider.visibility = View.GONE
        }
    }
}