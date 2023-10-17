package snu.swpp.moment.ui.calendar

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.children
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import snu.swpp.moment.R
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var monthTitleTextView: TextView

    private var selectedDate: LocalDate? = null

    private lateinit var monthEmotions: List<Int>
    private val emotionImageResources: List<Int> = listOf(
        R.drawable.icon_sunny,
        R.drawable.icon_sun_cloud,
        R.drawable.icon_cloud,
        R.drawable.icon_rain,
        R.drawable.icon_lightning,
    )

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
        val imageView: ImageView = view.findViewById(R.id.calendarDayImage)

        fun initialize(day: CalendarDay) {
            // 날짜 선택 시 동작
            view.setOnClickListener {
                if (day.position != DayPosition.MonthDate) {
                    return@setOnClickListener
                }

                val currentSelection = selectedDate
                if (currentSelection == day.date) {
                    // 날짜 선택 해제 후 reload
                    // FIXME: 선택 해제 뺄 거면 이 부분 뺴기
                    selectedDate = null
                    calendarView.notifyDateChanged(currentSelection)
                } else {
                    // 날짜 선택
                    selectedDate = day.date
                    calendarView.notifyDateChanged(day.date)
                    if (currentSelection != null) {
                        calendarView.notifyDateChanged(currentSelection)
                    }
                }
            }
        }
    }

    inner class MonthViewContainer(view: View) : ViewContainer(view) {
        val titlesContainer = view as ViewGroup
    }

    inner class DayInfoContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.dayInfoTextView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // Basic setup
        calendarView = findViewById(R.id.calendarView)
        monthTitleTextView = findViewById(R.id.monthTitleTextView)

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100)
        val firstDayOfWeek = firstDayOfWeekFromLocale()
        calendarView.setup(startMonth, endMonth, firstDayOfWeek)

        loadEmotions(currentMonth)
        calendarView.scrollToMonth(currentMonth)

        // Month header (Day of week titles)
        val daysOfWeek = daysOfWeek(firstDayOfWeek)
        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
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

//        // Month footer (Day info)
//        calendarView.monthFooterBinder = object : MonthHeaderFooterBinder<DayInfoContainer> {
//            override fun create(view: View) = DayInfoContainer(view)
//
//            override fun bind(container: DayInfoContainer, data: CalendarMonth) {
//                val currentSelection = selectedDate
//                if (currentSelection != null) {
//                    container.textView.text = "%d년 %d월 %d일".format(
//                        currentSelection.year,
//                        currentSelection.monthValue,
//                        currentSelection.dayOfMonth
//                    )
//                } else {
//                    container.textView.text = ""
//                }
//            }
//        }

        // Month scroll listener
        calendarView.monthScrollListener = { month ->
            // Update month title
            monthTitleTextView.text =
                "%d. %d.".format(month.yearMonth.year, month.yearMonth.monthValue)
            // Load emotion data
            loadEmotions(month.yearMonth)
        }

        // Day entries
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.initialize(data)

                // 날짜 text
                container.textView.text = data.date.dayOfMonth.toString()
                if (data.date == selectedDate) {
                    // 선택된 날짜는 빨간 색으로 표시
                    container.textView.setTextColor(Color.RED)
                } else {
                    container.textView.setTextColor(Color.BLACK)
                }

                if (data.position == DayPosition.MonthDate) {
                    // 감정 아이콘
                    container.imageView.setImageResource(emotionImageResources[monthEmotions[data.date.dayOfMonth - 1]])
                } else {
                    // 이전/다음 달의 날짜는 회색으로 표시 & 이미지 숨김
                    container.textView.setTextColor(Color.LTGRAY)
                    container.imageView.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun loadEmotions(month: YearMonth) {
        // API 호출해서 각 달의 감정 정보 가져오기
        monthEmotions = List(31, { i -> i % emotionImageResources.size })
    }
}