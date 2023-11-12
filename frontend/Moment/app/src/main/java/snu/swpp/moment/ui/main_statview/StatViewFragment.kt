package snu.swpp.moment.ui.main_statview

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView

import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import eu.wewox.tagcloud.TagCloud
import eu.wewox.tagcloud.rememberTagCloudState
import snu.swpp.moment.R
import snu.swpp.moment.data.repository.AuthenticationRepository
import snu.swpp.moment.data.repository.StoryRepository
import snu.swpp.moment.data.source.StoryRemoteDataSource
import snu.swpp.moment.databinding.FragmentStatviewBinding
import snu.swpp.moment.databinding.StatButtonDateBinding
import snu.swpp.moment.databinding.StatDurationBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class StatViewFragment : Fragment() {
    private lateinit var binding: FragmentStatviewBinding
    private lateinit var viewModel: StatViewModel
    private lateinit var lineChart: LineChart
    private val authenticationRepository: AuthenticationRepository =
        AuthenticationRepository.getInstance(context)
    private val storyRepository: StoryRepository = StoryRepository(StoryRemoteDataSource())
    //private lateinit var emotionColors: Map<String, Int>
    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(
            this,
            StatViewModelFactory(authenticationRepository, storyRepository)
        )[StatViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val statViewModel = ViewModelProvider(this).get(
            StatViewModel::class.java

        )

        binding = FragmentStatviewBinding.inflate(inflater, container, false)
        binding.statWordCloud.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                WordCloudView(mapOf())

            }
        }
        binding.statWordCloud.setOnTouchListener { v, event ->
            Log.d("touch2", "touch")
            binding.con.requestDisallowInterceptTouchEvent(true)
            return@setOnTouchListener false

        }
        val root: View = binding.root
        lineChart = binding.statLineChart

        // Include된 버튼, 기간을 포함하는 레이아웃에 대한 바인딩 객체 생성
        val buttonDateBinding =
            StatButtonDateBinding.bind(root.findViewById(R.id.statUtilContainer))
        val durationBinding =
            StatDurationBinding.bind(root.findViewById(R.id.stat_duration_container))
        // 기간 설정을 위한 코드
        viewModel.startDate.observe(viewLifecycleOwner) { date ->
            durationBinding.statDateDurationStart.text = formatDateString(date)
        }

        viewModel.endDate.observe(viewLifecycleOwner) { date ->
            durationBinding.statDateDurationEnd.text = formatDateString(date)
        }

        //버튼의 상태 (button press 관리)
        viewModel.selectedButtonType.observe(viewLifecycleOwner) { buttonType ->
            when (buttonType) {
                StatViewModel.ButtonType.WEEK -> {
                    buttonDateBinding.statWeekButton.isActivated = true
                    buttonDateBinding.statMonthButton.isActivated = false
                    binding.averageScoreText.text = getString(R.string.stat_seven_avg)
                }

                StatViewModel.ButtonType.MONTH -> {
                    buttonDateBinding.statWeekButton.isActivated = false
                    buttonDateBinding.statMonthButton.isActivated = true
                    binding.averageScoreText.text = getString(R.string.stat_thirty_avg)
                }
            }
        }

        buttonDateBinding.statWeekButton.setOnClickListener {
            statViewModel.getStats(false)
        }

        buttonDateBinding.statMonthButton.setOnClickListener {
            statViewModel.getStats(true)
        }

        // 점수 평균값
        // Observe the average score LiveData and update the UI
        viewModel.averageScore.observe(viewLifecycleOwner) { average ->
            val averageScoreTextView: TextView = root.findViewById(R.id.average_score_text_view)
            // Assuming you want to display the average score to one decimal place
            averageScoreTextView.text = getString(R.string.average_score_text, String.format("%.1f", average))
        }

        viewModel.stat.observe(viewLifecycleOwner) { state ->
            // Fragment가 활성 상태일 때만 UI 업데이트를 진행합니다.
            state?.let {
                scoreSetup(it.scoresBydateOffset, viewModel.today.value ?: LocalDate.now())
                //    hashtagSetup(it.hashtagCounts)
                emotionSetup(it.emotionCounts)
                //tagcloud가 값이 업데이트가 잘 안됨 따라서 완전히 새로운 view를 넣어야 함
                binding.frame.removeAllViews()
                val newCloud = ComposeView(requireContext())
                newCloud.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )

                binding.frame.addView(newCloud)
                newCloud.setContent {
                    WordCloudView(labels = it.hashtagCounts)
                }
            }
        }
        viewModel.getStats(false)

        return root
    }

    fun scoreSetup(scores: Map<Int, Int>, today: LocalDate) {
        class DateAxisValueFormat(today: LocalDate) : IndexAxisValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val formatter = DateTimeFormatter.ofPattern("MM/dd")
                return formatter.format(today.plusDays(Math.round(value).toLong()))
            }
        }

        val entries: MutableList<Entry> = mutableListOf()
        Log.d("stat_test : numentries", entries.size.toString())
        scores.forEach { day, score ->
            entries.add(Entry((-day).toFloat(), score.toFloat()))
            Log.d("stat_test", day.toString())
        }
        val dataset = LineDataSet(entries, null)

        // 그리드 dash
        val commonColor = android.graphics.Color.BLACK
        val commonLineWidth = 1.2f

        dataset.circleColors = listOf(requireContext().getColor(R.color.red))
        dataset.color = requireContext().getColor(R.color.black)
        dataset.setCircleRadius(3.5f)
        dataset.setDrawCircleHole(false)
        dataset.setDrawValues(false)

        lineChart.description = null
        lineChart.legend.isEnabled = false
        lineChart.isScaleYEnabled = false

        lineChart.axisLeft.axisLineWidth = commonLineWidth // Example value, adjust as necessary
        lineChart.axisLeft.axisLineColor = commonColor

        lineChart.axisRight.axisLineWidth =
            commonLineWidth // Assuming you want to make the right axis line bold as well
        lineChart.axisRight.axisLineColor = commonColor

        lineChart.xAxis.axisLineWidth = commonLineWidth
        lineChart.xAxis.axisLineColor = commonColor

        //grid
        lineChart.xAxis.enableGridDashedLine(
            10f,
            10f,
            0f
        ) // Example values for line length, space length, and phase
        lineChart.axisLeft.enableGridDashedLine(10f, 10f, 0f)
        lineChart.axisRight.enableGridDashedLine(
            10f,
            10f,
            0f
        ) // Example values for line length, space length, and phase

        //lineChart.axisRight.isEnabled = false
        lineChart.xAxis.valueFormatter = DateAxisValueFormat(today)
        lineChart.xAxis.isGranularityEnabled = true
        lineChart.xAxis.granularity = 1F
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        lineChart.axisLeft.axisMinimum = 0.0F
        lineChart.axisLeft.axisMaximum = 6.0F
        lineChart.axisRight.axisMinimum = 0.0F
        lineChart.axisRight.axisMaximum = 6.0F
        lineChart.axisLeft.setLabelCount(7, true);
        lineChart.axisLeft.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.maruburi_light)
        lineChart.axisRight.setLabelCount(7, true);

        dataset.setDrawHorizontalHighlightIndicator(false);
        dataset.setDrawVerticalHighlightIndicator(false);
        lineChart.xAxis.setLabelCount(entries.size, false)
        Log.d("stat_view", entries.size.toString())
        lineChart.xAxis.typeface = ResourcesCompat.getFont(requireContext(), R.font.maruburi_light)

        // 라인차트의 테두리를 그리기 위해 더미값을 넣고 해당 값은 출력안함
        lineChart.axisLeft.valueFormatter = (object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                if (value == 0f || value == 6f) {
                    return ""
                }
                return value.toInt().toString()
            }
        })

        lineChart.axisRight.valueFormatter = (object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return ""
            }
        })

        lineChart.axisLeft.gridColor = android.graphics.Color.BLACK
        val limitLine = LimitLine(6f, "").apply {
            lineWidth = commonLineWidth
            lineColor = commonColor
            enableDashedLine(0f, 0f, 0f) // 실선으로 설정 (대시 길이를 0으로)
        }

        // Y축에 LimitLine을 추가
        lineChart.axisLeft.addLimitLine(limitLine)
        lineChart.data = LineData(dataset)

        lineChart.fitScreen()
        lineChart

        // 7일, 30일 다른 간격 수를 보여줌
        if (scores.size <= 7) {
            lineChart.setVisibleXRange(6.0F, 6.0F) // 최대 5일치 데이터를 표시
        } else if (scores.size <= 30) {
            lineChart.setVisibleXRange(7.0F, 7.0F) // 최대 7일치 데이터를 표시
        }
        // 차트가 항상 최신 데이터를 표시하도록 설정합니다.
        lineChart.moveViewToX(dataset.xMax)

        // 아래 두줄은 linechart가 항상 오른쪽으로 스크롤 되어있도록
        lineChart.animateX(1000)
        lineChart.moveViewToX(dataset.xMax)

        lineChart.invalidate()
        dataset.notifyDataSetChanged()
    }

    fun emotionSetup(emotions: Map<String, Int>) {
        //val emotions = sampleEmotions()  // testset
        val categoryIcons = mapOf(
            "emotion_sunny" to R.drawable.small_icon_sunny,
            "emotion_sun_cloud" to R.drawable.small_icon_sun_cloud,
            "emotion_cloud" to R.drawable.small_icon_cloud,
            "emotion_rain" to R.drawable.small_icon_rain,
            "emotion_lightning" to R.drawable.small_icon_lightning
        )

        // Map the emotions to categories
        val emotionToCategory = mapOf(
            "설렘" to "emotion_sunny",
            "신남" to "emotion_sunny",
            "기쁨" to "emotion_sun_cloud",
            "행복" to "emotion_sun_cloud",
            "평범" to "emotion_cloud",
            "모름" to "emotion_cloud",
            "슬픔" to "emotion_rain",
            "우울" to "emotion_rain",
            "화남" to "emotion_lightning",
            "짜증" to "emotion_lightning"
        )

        // Create a map for aggregated emotion values

        val aggregatedEmotions = mutableMapOf<String, Int>()
        for ((emotion, value) in emotions) {
            val category = emotionToCategory[emotion] ?: continue
            aggregatedEmotions.merge(category, value, Int::plus)
        }

        val pieEntries: MutableList<PieEntry> = mutableListOf()
        val colors: MutableList<Int> = mutableListOf()
        for ((category, sumValue) in aggregatedEmotions) {
            if (sumValue > 0) { // Check if the value is greater than 0
                val pieEntry = PieEntry(sumValue.toFloat(), category)
                categoryIcons[category]?.let { iconId ->
                    val drawable = ResourcesCompat.getDrawable(requireContext().resources, iconId, null)
                    drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                    pieEntry.icon = drawable
                }
                pieEntries.add(pieEntry)
                colors.add(getEmotionColor_grayScale(category)) // Use the first emotion in the category for color
            }
        }
        val pieChart = binding.statPieChart
        val pieDataSet = PieDataSet(pieEntries, "").apply {
            this.colors = colors
            setDrawIcons(true)
            valueTextColor = requireContext().getColor(R.color.white)
            valueTextSize = 0f
            valueTypeface = ResourcesCompat.getFont(requireContext(), R.font.maruburi_bold)
            setSliceSpace(1f) // space between slices
        }

        // Configure the pie chart
        with(pieChart) {
            pieChart.renderer = CustomPieChartRenderer(pieChart,pieChart.animator,pieChart.viewPortHandler)
            data = PieData(pieDataSet)
            legend.isEnabled = false // Disable the legend
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
                textSize = 12f
                typeface = ResourcesCompat.getFont(requireContext(), R.font.maruburi_bold)
                form = Legend.LegendForm.CIRCLE

            }
            description.isEnabled = false
            setDrawEntryLabels(false)
            invalidate() // refresh the chart with new data
        }

    }

    fun sampleEmotions(): Map<String, Int> {//FIXME: test 후 지우기
        val m: MutableMap<String, Int> = mutableMapOf()
        m["기쁨"] = 4
        m["설렘"] = 8
        m["행복"] = 3
        m["신남"] = 2
        m["평범"] = 5
        m["모름"] = 2
        m["짜증"] = 3
        m["화남"] = 3
        m["슬픔"] = 4
        m["우울"] = 2
        return m

    }



    fun getEmotionColor_grayScale(category: String): Int {
        if (category == "emotion_sunny")
            return requireContext().getColor(R.color.stat_emotion_1)
        else if (category == "emotion_sun_cloud")
            return requireContext().getColor(R.color.stat_emotion_3)
        else if (category == "emotion_cloud")
            return requireContext().getColor(R.color.stat_emotion_5)
        else if (category == "emotion_rain")
            return requireContext().getColor(R.color.stat_emotion_7)
        else
            return requireContext().getColor(R.color.stat_emotion_9)
    }


    private fun formatDateString(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("yy.MM.dd"))
    }

    @Composable
    fun WordCloudView(labels: Map<String, Int>) {
        TagCloud(
            state = rememberTagCloudState(),
            modifier = Modifier
                .padding(64.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            binding.root.requestDisallowInterceptTouchEvent(true)
                        }
                    )
                }
        ) {
            items((labels.toList())) {
                Surface(
                    shape = RoundedCornerShape(7.dp),
                    color = colorResource(id = R.color.darkgray),
                    modifier = Modifier
                        .tagCloudItemFade()
                        .tagCloudItemScaleDown()
                ) {
                    Text(
                        text = "#"+it.first,
                        //    color = Color.BLUE,
                        modifier = Modifier.padding(2.dp).padding(start = 5.dp, end = 5.dp),
                        fontFamily = FontFamily(Font(R.font.maruburi_bold)),
                        color = androidx.compose.ui.graphics.Color.White

                    )
                }
            }
        }
    }

}