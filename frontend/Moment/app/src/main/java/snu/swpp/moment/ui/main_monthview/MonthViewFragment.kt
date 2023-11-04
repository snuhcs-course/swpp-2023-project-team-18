package snu.swpp.moment.ui.main_monthview

import android.content.Context
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
import com.kizitonwose.calendar.view.MarginValues
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import snu.swpp.moment.MainActivity
import snu.swpp.moment.data.repository.AuthenticationRepository
import snu.swpp.moment.data.repository.StoryRepository
import snu.swpp.moment.data.source.StoryRemoteDataSource
import snu.swpp.moment.databinding.FragmentMonthviewBinding
import snu.swpp.moment.utils.isFinalWeekOfMonth
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class MonthViewFragment : Fragment() {

    private lateinit var binding: FragmentMonthviewBinding

    private val authenticationRepository: AuthenticationRepository =
        AuthenticationRepository.getInstance(context)
    private val storyRepository: StoryRepository = StoryRepository(StoryRemoteDataSource())
    private lateinit var viewModel: MonthViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(
            this,
            MonthViewModelFactory(authenticationRepository, storyRepository)
        )[MonthViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMonthviewBinding.inflate(inflater, container, false)
        val view = binding.root

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100)
        val firstDayOfWeek = firstDayOfWeekFromLocale()
        binding.calendarView.setup(startMonth, endMonth, firstDayOfWeek)

        viewModel.setCurrentMonth(currentMonth)
        binding.calendarView.scrollToMonth(currentMonth)

        viewModel.observerCalendarMonthState {
            if (it == null) {
                return@observerCalendarMonthState
            }
            Log.d("MonthViewFragment", "calendarMonthState changed -> notifyCalendarChanged()")
            binding.calendarView.notifyCalendarChanged()
        }

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
        binding.calendarView.dayBinder = object : MonthDayBinder<CalendarDayContainer> {
            override fun create(view: View) = CalendarDayContainer(view)

            override fun bind(container: CalendarDayContainer, data: CalendarDay) {
                container.initialize(binding.calendarView, viewModel, data)

                val dayOfMonth = data.date.dayOfMonth
                container.updateDateText(dayOfMonth, data.date == viewModel.getSelectedDate())

                if (data.position != DayPosition.MonthDate) {
                    container.setUiOutDate()
                } else if (data.date.monthValue != viewModel.getCurrentMonth().monthValue) {
                    container.setUiScrolling()
                } else {
                    val calendarDayState = viewModel.getDayState(dayOfMonth)

                    if (calendarDayState == null) {
                        // 처음에 아직 API response 안 옴
                        Log.d("MonthViewFragment", "calendarDayState is null (day $dayOfMonth)")
                    } else if (!viewModel.monthChangedSwitch) {
                        // 아직 API response 안 옴
                        Log.d("MonthViewFragment", "monthChangedSwitch is false (day $dayOfMonth)")
                    } else {
                        container.setUiMonthDate(calendarDayState)
                        Log.d(
                            "MonthViewFragment",
                            "setUiMonthDate (day $dayOfMonth, ${calendarDayState.isEmotionInvalid})"
                        )
                    }
                }
                container.setDividerLineVisible(!isFinalWeekOfMonth(data))
            }
        }

        // 달력 아래 요약 정보
        viewModel.observerSelectedDayState {
            if (it == null) {
                return@observerSelectedDayState
            }

            binding.daySummaryContainer.root.visibility = View.VISIBLE
            binding.daySummaryContainer.daySummaryDateText.text = it.dateText
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

        binding.daySummaryContainer.dayNavigateButton.isActivated = true
        binding.daySummaryContainer.dayNavigateButton.setOnClickListener {
            (requireActivity() as MainActivity).navigateToWriteViewPage(viewModel.getSelectedDate())
        }

        return view
    }

    // 요일 보여주는 부분
    inner class MonthViewContainer(view: View) : ViewContainer(view) {
        val titlesContainer = view as ViewGroup
    }
}