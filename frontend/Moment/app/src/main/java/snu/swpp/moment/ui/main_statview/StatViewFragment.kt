package snu.swpp.moment.ui.main_statview

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import net.alhazmy13.wordcloud.WordCloud
import snu.swpp.moment.R
import snu.swpp.moment.data.repository.AuthenticationRepository
import snu.swpp.moment.data.repository.StoryRepository
import snu.swpp.moment.data.source.StoryRemoteDataSource
import snu.swpp.moment.databinding.FragmentStatviewBinding
import snu.swpp.moment.databinding.StatButtonDateBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class StatViewFragment : Fragment() {
    private lateinit var binding: FragmentStatviewBinding
    private lateinit var viewModel: StatViewModel
    private lateinit var lineChart:LineChart
    private val authenticationRepository: AuthenticationRepository =
        AuthenticationRepository.getInstance(context)
    private val storyRepository: StoryRepository = StoryRepository(StoryRemoteDataSource())
    private lateinit var emotionColors:Map<String,Int>
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
        val root: View = binding.root
        lineChart = binding.statLineChart


        // Include된 버튼, 기간을 포함하는 레이아웃에 대한 바인딩 객체 생성
        val buttonDateBinding = StatButtonDateBinding.bind(root.findViewById(R.id.statUtilContainer))

        //버튼의 상태
        viewModel.selectedButtonType.observe(viewLifecycleOwner) { buttonType ->
            when (buttonType) {
                StatViewModel.ButtonType.WEEK -> {
                    buttonDateBinding.statWeekButton.isActivated = true
                    buttonDateBinding.statMonthButton.isActivated = false
                }
                StatViewModel.ButtonType.MONTH -> {
                    buttonDateBinding.statWeekButton.isActivated = false
                    buttonDateBinding.statMonthButton.isActivated = true
                }
            }
        }

        buttonDateBinding.statWeekButton.setOnClickListener {
            statViewModel.getStats(false)
        }

        buttonDateBinding.statMonthButton.setOnClickListener {
            statViewModel.getStats(true)
        }

        viewModel.stat.observeForever{
            val state = viewModel.stat.value
            val today = viewModel.today.value
            state?.let {
                today?.let {
                    scoreSetup(state.scoresBydateOffset, today)
                    hashtagSetup(state.hashtagCounts)
                    emotionSetup(state.emotionCounts)
                }
            }
        }
        viewModel.getStats(false)



        return root
    }
    fun scoreSetup(scores:Map<Int,Int>,today: LocalDate){
        class DateAxisValueFormat(today:LocalDate) : IndexAxisValueFormatter() {

            override fun getFormattedValue(value: Float): String {
                val formatter = DateTimeFormatter.ofPattern("MM/dd")
                return formatter.format(today.plusDays(Math.round(value).toLong()))
            }
        }
        val entries:MutableList<Entry> = mutableListOf()
        scores.forEach { day, score ->
            entries.add(Entry((-day).toFloat(), score.toFloat()))
        }
        val dataset = LineDataSet(entries,null)
        val assetManager = requireContext().assets
        dataset.circleColors = listOf(requireContext().getColor(R.color.black))
        dataset.color = requireContext().getColor(R.color.black)
        dataset.setDrawCircleHole(false)
        dataset.setDrawValues(false)

        lineChart.description = null
        lineChart.legend.isEnabled = false
        lineChart.isScaleYEnabled = false

        lineChart.xAxis.valueFormatter = DateAxisValueFormat(today)
        lineChart.xAxis.isGranularityEnabled = true
        lineChart.xAxis.granularity = 1.0F
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.axisLeft.axisMinimum = 0.0F
        lineChart.axisLeft.axisMaximum = 5.0F
        lineChart.axisRight.axisMinimum = 0.0F
        lineChart.axisRight.axisMaximum = 5.0F
        lineChart.axisLeft.setLabelCount(6,true);
        lineChart.axisLeft.typeface = ResourcesCompat.getFont(requireContext(),R.font.maruburi_light)
        lineChart.axisRight.setLabelCount(6,true);
        lineChart.axisRight.isEnabled = false
        dataset.setDrawHorizontalHighlightIndicator(false);
        dataset.setDrawVerticalHighlightIndicator(false);
        lineChart.xAxis.setLabelCount(entries.size,false)
        lineChart.xAxis.typeface = ResourcesCompat.getFont(requireContext(),R.font.maruburi_light)


        lineChart.axisLeft.valueFormatter = (object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        })


        lineChart.data = LineData(dataset)

        lineChart.fitScreen()
        lineChart
        lineChart.setVisibleXRange(5.0F,5.0F)

        lineChart.invalidate()
        dataset.notifyDataSetChanged()



    }
    fun hashtagSetup(hashtags:Map<String,Int>){
        val wordCloudView = binding.statWordCloud

        class MyJavaScriptInterface {
            @JavascriptInterface
            fun calculateContentDimensions(contentWidth: Int, contentHeight: Int) {
                // Calculate the desired zoom level to fit the content
                val zoomLevel = wordCloudView.width as Float / contentWidth
                activity!!.runOnUiThread(Runnable { // Set the zoom level of the WebView to fit the content
                    wordCloudView.setInitialScale((zoomLevel * 100).toInt())
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
            //  wordCloudView.setScale(30,10)
     //   wordCloudView.setColors(intArrayOf(Color.BLUE))
       wordClouds.add(WordCloud("",0))
      //  wordClouds.add(WordCloud("기ㅏㄴㅇ렁니ㅏ러;ㄴㅇ러;ㄹㅎㅓ;ㅁ",2))

        wordCloudView.setDataSet(wordClouds)
        Log.d("data2",wordCloudView.data)
        wordCloudView.notifyDataSetChanged()
      /*  wordCloudView.addJavascriptInterface( MyJavaScriptInterface(),"Android");
        wordCloudView.webViewClient = object: WebViewClient() {

            override fun onPageFinished(view: WebView, url:String) {
                // Call a JavaScript function to calculate the content dimensions
                view.loadUrl("javascript:Android.calculateContentDimensions(document.body.scrollWidth, document.body.scrollHeight);");
            }
        }
        wordCloudView.setOnTouchListener { v, event ->
            e*/




    }
    fun emotionSetup(emotions:Map<String,Int>){
        val pieChart = binding.statPieChart
        var pie : MutableList<PieEntry> = mutableListOf()
        var colors:MutableList<Int> = mutableListOf()
        val emotions = sampleEmotions()
        for(emotion in emotions){
            pie.add(PieEntry(emotion.value.toFloat(),emotion.key))
            colors.add(getEmotionColor(emotion.key))

        }
        val pieDataset = PieDataSet(pie,"")
        pieDataset.colors = colors
        pieChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        pieChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        pieChart.legend.orientation = Legend.LegendOrientation.VERTICAL
        pieChart.legend.setDrawInside(false);
        pieChart.legend.textSize = 12f
        pieChart.legend.typeface = ResourcesCompat.getFont(requireContext(),R.font.maruburi_light)
        pieChart.legend.form = Legend.LegendForm.CIRCLE
        pieChart.description.isEnabled = false
        pieChart.setDrawEntryLabels(false)
        pieDataset.valueFormatter = object: ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value / emotions.size * 100} %"
            }
        }
        pieDataset.valueTextColor = requireContext().getColor(R.color.white)
        pieDataset.valueTextSize = 12f
        pieDataset.valueTypeface = ResourcesCompat.getFont(requireContext(),R.font.maruburi_bold)



        pieChart.data = PieData(pieDataset)
        pieChart.invalidate()

    }
    fun sampleEmotions():Map<String,Int>{//FIXME: test 후 지우기
        val m:MutableMap<String,Int> = mutableMapOf()
        m["기쁨"]=1
        m["설렘"]=2
        m["행복"]=1
        m["신남"]=1
        m["평범"]=1
        m["모름"]=1
        m["짜증"]=1
        m["화남"]=1
    //    m["슬픔"]=1
     //   m["우울"]=1
        return m

    }
    fun getEmotionColor(emotion:String):Int{
        if(emotion.equals("설렘"))
            return requireContext().getColor(R.color.stat_emotion_1)
        else if(emotion.equals("신남"))
            return requireContext().getColor(R.color.stat_emotion_2)
        else if(emotion.equals("기쁨"))
            return requireContext().getColor(R.color.stat_emotion_3)
        else if(emotion.equals("행복"))
            return requireContext().getColor(R.color.stat_emotion_4)
        else if(emotion.equals("평범"))
            return requireContext().getColor(R.color.stat_emotion_5)
        else if(emotion.equals("모름"))
            return requireContext().getColor(R.color.stat_emotion_6)
        else if(emotion.equals("슬픔"))
            return requireContext().getColor(R.color.stat_emotion_7)
        else if(emotion.equals("우울"))
            return requireContext().getColor(R.color.stat_emotion_8)
        else if(emotion.equals("화남"))
            return requireContext().getColor(R.color.stat_emotion_9)
        else
            return requireContext().getColor(R.color.stat_emotion_10)


    }
    override fun onDestroyView() {
        super.onDestroyView()
    }
}