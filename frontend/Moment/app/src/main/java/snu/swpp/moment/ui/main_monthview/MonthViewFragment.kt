package snu.swpp.moment.ui.main_monthview

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MarginValues
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import snu.swpp.moment.MainActivity
import snu.swpp.moment.R
import snu.swpp.moment.databinding.FragmentMonthviewBinding
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class MonthViewFragment : Fragment() {

    companion object {
        fun newInstance() = MonthViewFragment()
    }

    private lateinit var viewModel: CalendarViewModel
    private lateinit var binding: FragmentMonthviewBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMonthviewBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel = ViewModelProvider(this)[CalendarViewModel::class.java]

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100)
        val firstDayOfWeek = firstDayOfWeekFromLocale()
        binding.calendarView.setup(startMonth, endMonth, firstDayOfWeek)

        viewModel.setCurrentMonth(currentMonth)
        binding.calendarView.scrollToMonth(currentMonth)
        // 스크롤 할 때 각 달 사이의 간격
        val monthMargin = 10
        binding.calendarView.monthMargins = MarginValues(monthMargin, 0, monthMargin, 0)

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

        // Month scroll listener (화면 위 연도/달 표시)
        binding.calendarView.monthScrollListener = { month ->
            // Update month title
            viewModel.setCurrentMonth(month.yearMonth)
            binding.calendarView.notifyCalendarChanged()

            // main activity의 tool bar title 변경
            val activity = activity as MainActivity
            val yearMonthText = "%d. %d.".format(month.yearMonth.year, month.yearMonth.monthValue)
            activity.setToolbarTitle(yearMonthText)
        }

        // 각 날짜 칸
        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.initialize(binding.calendarView, viewModel, data)
                if (isLastWeek(data)) {
                    // 마지막 줄 구분선 제거
                    container.divider.visibility = View.GONE
                } else {
                    container.divider.visibility = View.VISIBLE
                }

                // 날짜 text
                container.textView.text = data.date.dayOfMonth.toString()
                if (data.date == viewModel.selectedDate.value) {
                    // 선택된 날짜는 빨간 색으로 표시
                    container.textView.setTextColor(ContextCompat.getColor(context!!, R.color.red))
                    val typeface: Typeface =
                        ResourcesCompat.getFont(context!!, R.font.maruburi_bold)!!
                    container.textView.typeface = typeface
                } else {
                    container.textView.setTextColor(
                        ContextCompat.getColor(
                            context!!,
                            R.color.black
                        )
                    )
                    val typeface: Typeface =
                        ResourcesCompat.getFont(context!!, R.font.maruburi_light)!!
                    container.textView.typeface = typeface
                }

                if (data.position != DayPosition.MonthDate) {
                    // 이전/다음 달의 날짜는 회색으로 표시 & 이미지 숨김
                    container.textView.setTextColor(ContextCompat.getColor(context!!, R.color.gray))
                    container.imageView.visibility = View.INVISIBLE
                    container.autoCompletedDot.visibility = View.GONE
                } else if (data.date.monthValue != viewModel.currentMonth.value!!.monthValue) {
                    // 스크롤 도중 이전/다음 달은 내용 안 보여줌
                    container.imageView.setImageResource(android.R.color.transparent)
                    container.autoCompletedDot.visibility = View.GONE
                } else {
                    // 감정 아이콘
                    if (viewModel.calendarDayStates.value != null) {
                        val calendarDayState =
                            viewModel.calendarDayStates.value!![data.date.dayOfMonth - 1]
                        container.imageView.setImageResource(calendarDayState.emotionImage)
                        container.imageView.visibility = View.VISIBLE
                        container.autoCompletedDot.visibility =
                            if (calendarDayState.isAutoCompleted) View.VISIBLE else View.GONE
                    }
                }
            }
        }

        // 달력 아래 요약 정보
        val daySummaryObserver = Observer<CalendarDayInfoState?> { it ->
            if (it != null) {
                binding.daySummaryContainer.root.visibility = View.VISIBLE
                binding.daySummaryContainer.daySummaryDateText.text = "%d. %d. %d. %s".format(
                    it.date.year,
                    it.date.monthValue,
                    it.date.dayOfMonth,
                    it.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN),
                )
                binding.daySummaryContainer.dayStoryTitleText.text = it.storyTitle
                binding.daySummaryContainer.dayStoryContentText.text = it.storyContent
                binding.daySummaryContainer.dayEmotionImage.setImageResource(
                    it.emotionImage
                )
                binding.daySummaryContainer.dayEmotionText.text = it.emotionKoreanText
                binding.daySummaryContainer.dayTagsText.text = it.tags.joinToString(" ")
                binding.daySummaryContainer.dayScoreText.text = it.score.toString()

                if (it.isAutoCompleted) {
                    binding.daySummaryContainer.infoAutoCompletedText.visibility = View.VISIBLE
                    binding.daySummaryContainer.infoAutoCompletedDot.visibility = View.VISIBLE
                } else {
                    binding.daySummaryContainer.infoAutoCompletedText.visibility = View.INVISIBLE
                    binding.daySummaryContainer.infoAutoCompletedDot.visibility = View.GONE
                }
            }
        }
        viewModel.calendarDayInfoState.observe(viewLifecycleOwner, daySummaryObserver)

        return view
    }

    // 요일 보여주는 부분
    inner class MonthViewContainer(view: View) : ViewContainer(view) {
        val titlesContainer = view as ViewGroup
    }
}