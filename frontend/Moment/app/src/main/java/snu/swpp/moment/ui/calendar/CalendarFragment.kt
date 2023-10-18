package snu.swpp.moment.ui.calendar

import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import snu.swpp.moment.R
import snu.swpp.moment.databinding.FragmentCalendarBinding
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class CalendarFragment : Fragment() {

    companion object {
        fun newInstance() = CalendarFragment()
    }

    private lateinit var viewModel: CalendarViewModel
    private lateinit var binding: FragmentCalendarBinding

    private val emotionImageResources: List<Int> = listOf(
        R.drawable.icon_sunny,
        R.drawable.icon_sunny,
        R.drawable.icon_sun_cloud,
        R.drawable.icon_sun_cloud,
        R.drawable.icon_cloud,
        R.drawable.icon_cloud,
        R.drawable.icon_rain,
        R.drawable.icon_rain,
        R.drawable.icon_lightning,
        R.drawable.icon_lightning,
        android.R.color.transparent
    )
    fun isLastWeek(day:CalendarDay):Boolean{
        val date = day.date
        if(day.position == DayPosition.InDate)return false;

        if(day.position == DayPosition.OutDate){
            val firstSat = date.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
            return date.isAfter(firstSat.plusDays(28))

        }
        if(day.position == DayPosition.MonthDate){
            val firstSat = date.with(TemporalAdjusters.firstDayOfMonth()).with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
            return date.isAfter(firstSat.plusDays(28))

        }
    return false;
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel = ViewModelProvider(this)[CalendarViewModel::class.java]

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100)
        val firstDayOfWeek = firstDayOfWeekFromLocale()
        binding.calendarView.setup(startMonth, endMonth, firstDayOfWeek)

        viewModel.setCurrentMonth(currentMonth)
        binding.calendarView.scrollToMonth(currentMonth)

        // Month header (요일 보여주는 부분)
        val daysOfWeek = daysOfWeek(firstDayOfWeek)
        binding.calendarView.monthHeaderBinder = object :
            MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)

            override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                if (container.titlesContainer.tag == null) {
                    container.titlesContainer.tag = data.yearMonth
                    container.titlesContainer.children.map { it as TextView }
                        .forEachIndexed { index, textView ->
                            val dayOfWeek = daysOfWeek[index]
                            val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
                            textView.text = title
                        }
                }
            }
        }

        // Month scroll listener
        binding.calendarView.monthScrollListener = { month ->
            // Update month title
            binding.monthTitleTextView.monthTitleText.text =
                "%d. %d.".format(month.yearMonth.year, month.yearMonth.monthValue)
            viewModel.setCurrentMonth(month.yearMonth)
        }

        // 각 날짜 칸
        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.initialize(binding.calendarView, viewModel, data)
                if(isLastWeek(data)){
                    container.divider.visibility = View.GONE
                }
                else{

                    container.divider.visibility = View.VISIBLE
                }

                // 날짜 text
                container.textView.text = data.date.dayOfMonth.toString()
                if (data.date == viewModel.selectedDate.value) {
                    // 선택된 날짜는 빨간 색으로 표시
                    container.textView.setTextColor(Color.RED)
                } else {
                    container.textView.setTextColor(Color.BLACK)
                }

                if (data.position == DayPosition.MonthDate) {
                    // 감정 아이콘
                    if (viewModel.monthState.value != null) {
                        container.imageView.setImageResource(emotionImageResources[viewModel.monthState.value!![data.date.dayOfMonth - 1].emotion])
                        container.completionView.visibility = if (viewModel.monthState.value!![data.date.dayOfMonth - 1].isAutoCompleted)  View.VISIBLE else View.GONE
                    }
                } else {
                    // 이전/다음 달의 날짜는 회색으로 표시 & 이미지 숨김
                    container.textView.setTextColor(Color.LTGRAY)
                    container.imageView.visibility = View.INVISIBLE
                    container.completionView.visibility = View.GONE
                }
            }
        }
        viewModel.monthState.observe(viewLifecycleOwner){
            binding.calendarView.notifyCalendarChanged()
        }















        return view

    }

//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        viewModel = ViewModelProvider(this).get(CalendarViewModel::class.java)
//        // TODO: Use the ViewModel
//    }

    // 요일 보여주는 부분
    inner class MonthViewContainer(view: View) : ViewContainer(view) {
        val titlesContainer = view as ViewGroup
    }


}