package snu.swpp.moment.ui.main_statview

import android.content.Context
import android.graphics.DashPathEffect
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView

import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.semantics.SemanticsProperties.Text
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
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
import net.alhazmy13.wordcloud.WordCloud
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
    private lateinit var emotionColors: Map<String, Int>
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
                    binding.statDayScoreText.text = getString(R.string.stat_section_week_score)
                }

                StatViewModel.ButtonType.MONTH -> {
                    buttonDateBinding.statWeekButton.isActivated = false
                    buttonDateBinding.statMonthButton.isActivated = true
                    binding.statDayScoreText.text = getString(R.string.stat_section_month_score)
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
        viewModel.highestScore.observe(viewLifecycleOwner) { highest ->
            val highestScoreTextView: TextView = root.findViewById(R.id.highest_score_text_view)
            highestScoreTextView.text = getString(R.string.highest_score_text, highest)
        }

        // Observe the lowest score LiveData and update the UI
        viewModel.lowestScore.observe(viewLifecycleOwner) { lowest ->
            val lowestScoreTextView: TextView = root.findViewById(R.id.lowest_score_text_view)
            lowestScoreTextView.text = getString(R.string.lowest_score_text, lowest)
        }

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
        scores.forEach { day, score ->
            entries.add(Entry((-day).toFloat(), score.toFloat()))
        }
        val dataset = LineDataSet(entries, null)
        val assetManager = requireContext().assets

        // 그리드 dash
        val dashPathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
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
        lineChart.setVisibleXRange(5.0F, 5.0F)



        // 아래 두줄은 linechart가 항상 오른쪽으로 스크롤 되어있도록
        lineChart.animateX(1000)
        lineChart.moveViewToX(dataset.xMax)

        lineChart.invalidate()
        dataset.notifyDataSetChanged()


    }
    /* fun hashtagSetup(hashtags:Map<String,Int>){
         val wordCloudView = binding.statWordCloud

         class MyJavaScriptInterface {
             @JavascriptInterface
             fun calculateContentDimensions(contentWidth: Int, contentHeight: Int) {
                 // Calculate the desired zoom level to fit the content
                 val zoomLevel = (wordCloudView.width.toFloat()) / contentWidth
                 activity!!.runOnUiThread(Runnable { // Set the zoom level of the WebView to fit the content
                     wordCloudView.setInitialScale((zoomLevel * 50).toInt())
                 })
             }
         }

         val wordClouds:MutableList<WordCloud> = mutableListOf()
         wordCloudView.setScale(30,10)
         for(hashtag in hashtags){
             wordClouds.add(WordCloud(hashtag.key,1))
             Log.d("hashtag",hashtag.key)
             Log.d("weight",hashtag.value.toString())
         }
        wordClouds.add(WordCloud("",0))

         wordCloudView.setDataSet(wordClouds)
         wordCloudView.setColors(intArrayOf(Color.BLUE, Color.GRAY, Color.GREEN, Color.CYAN))

         Log.d("data2",wordCloudView.data)
         wordCloudView.addJavascriptInterface( MyJavaScriptInterface(),"Android");
         wordCloudView.notifyDataSetChanged()

         }*/


    fun emotionSetup(emotions: Map<String, Int>) {
        val pieChart = binding.statPieChart
        var pie: MutableList<PieEntry> = mutableListOf()
        var colors: MutableList<Int> = mutableListOf()
        val emotions = sampleEmotions()
        for (emotion in emotions) {
            pie.add(PieEntry(emotion.value.toFloat(), emotion.key))
            colors.add(getEmotionColor_grayScale(emotion.key))

        }

        // 추가한것, 테스트중
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)


        val pieDataset = PieDataSet(pie, "")
        pieDataset.colors = colors

        // 추가한것, 테스트중
        pieChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        //pieChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        pieChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        pieChart.legend.orientation = Legend.LegendOrientation.VERTICAL
        pieChart.legend.setDrawInside(false);
        pieChart.legend.textSize = 12f
        pieChart.legend.typeface = ResourcesCompat.getFont(requireContext(), R.font.maruburi_bold)
        pieChart.legend.form = Legend.LegendForm.CIRCLE
        pieChart.description.isEnabled = false

        // 아래 두 줄도 추가한 것. legned랑 파이차트 사이 멀지 않게
        pieChart.legend.xEntrySpace = 0f // Adjust the space between the legend entries and the pie chart
        pieChart.legend.yEntrySpace = 0f // Adjust vertical space if necessary

        pieChart.setDrawEntryLabels(false)
        pieDataset.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value / emotions.size * 100} %"
            }
        }
        pieDataset.valueTextColor = requireContext().getColor(R.color.white)
        pieDataset.valueTextSize = 12f
        pieDataset.valueTypeface = ResourcesCompat.getFont(requireContext(), R.font.maruburi_bold)



        pieChart.data = PieData(pieDataset)
        pieChart.invalidate()

    }

    fun sampleEmotions(): Map<String, Int> {//FIXME: test 후 지우기
        val m: MutableMap<String, Int> = mutableMapOf()
        m["기쁨"] = 1
        m["설렘"] = 2
        m["행복"] = 1
        m["신남"] = 1
        m["평범"] = 1
        m["모름"] = 1
        m["짜증"] = 1
        m["화남"] = 1
        return m

    }



    fun getEmotionColor_grayScale(emotion: String): Int {
        if (emotion.equals("설렘") || emotion.equals("신남"))
            return requireContext().getColor(R.color.stat_emotion_1)
        else if (emotion.equals("기쁨") || emotion.equals("행복"))
            return requireContext().getColor(R.color.stat_emotion_3)
        else if (emotion.equals("평범") || emotion.equals("모름"))
            return requireContext().getColor(R.color.stat_emotion_5)
        else if (emotion.equals("슬픔") || emotion.equals("우울"))
            return requireContext().getColor(R.color.stat_emotion_7)
        else
            return requireContext().getColor(R.color.stat_emotion_9)


    }
    fun getEmotionColor(emotion: String): Int {
        if (emotion.equals("설렘"))
            return requireContext().getColor(R.color.stat_emotion_1)
        else if (emotion.equals("신남"))
            return requireContext().getColor(R.color.stat_emotion_2)
        else if (emotion.equals("기쁨"))
            return requireContext().getColor(R.color.stat_emotion_3)
        else if (emotion.equals("행복"))
            return requireContext().getColor(R.color.stat_emotion_4)
        else if (emotion.equals("평범"))
            return requireContext().getColor(R.color.stat_emotion_5)
        else if (emotion.equals("모름"))
            return requireContext().getColor(R.color.stat_emotion_6)
        else if (emotion.equals("슬픔"))
            return requireContext().getColor(R.color.stat_emotion_7)
        else if (emotion.equals("우울"))
            return requireContext().getColor(R.color.stat_emotion_8)
        else if (emotion.equals("화남"))
            return requireContext().getColor(R.color.stat_emotion_9)
        else
            return requireContext().getColor(R.color.stat_emotion_10)
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier
                        .tagCloudItemFade()
                        .tagCloudItemScaleDown()
                ) {
                    Text(
                        text = it.first,
                        //    color = Color.BLUE,
                        modifier = Modifier.padding(2.dp),
                        color = androidx.compose.ui.graphics.Color.Blue


                    )
                }
            }
        }
    }

}