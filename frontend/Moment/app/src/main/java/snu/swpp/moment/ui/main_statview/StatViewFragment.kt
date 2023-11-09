package snu.swpp.moment.ui.main_statview

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
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


        binding.statWeekButton.setOnClickListener {
            statViewModel.getStats(false)
        }

        binding.statMonthButton.setOnClickListener {
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
        lineChart.description = null
        lineChart.legend.isEnabled = false

        lineChart.xAxis.valueFormatter = DateAxisValueFormat(today)
        lineChart.xAxis.isGranularityEnabled = true
        lineChart.xAxis.granularity = 1.0F
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.axisLeft.axisMinimum = 1.0F
        lineChart.axisLeft.axisMaximum = 5.0F
        lineChart.axisRight.axisMinimum = 1.0F
        lineChart.axisRight.axisMaximum = 5.0F
        lineChart.axisLeft.setLabelCount(5,true);
        lineChart.axisRight.setLabelCount(5,true);
        lineChart.axisRight.isEnabled = false
        dataset.setDrawHorizontalHighlightIndicator(false);
        dataset.setDrawVerticalHighlightIndicator(false);
        lineChart.xAxis.setLabelCount(entries.size,false)


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
        for(emotion in emotions){
            pie.add(PieEntry(emotion.value.toFloat(),emotion.key))
            colors.add(getEmotionColor(emotion.key))

        }
        val pieDataset = PieDataSet(pie,"감정")
        pieDataset.colors = colors
        pieChart.data = PieData(pieDataset)
        pieChart.invalidate()

    }
    fun getEmotionColor(emotion:String):Int{
        if(emotion.equals("설렘")||emotion.equals("신남"))
            return requireContext().getColor(R.color.stat_emotion_12)
        else if(emotion.equals("기쁨")||emotion.equals("행복"))
            return requireContext().getColor(R.color.stat_emotion_34)
        else if(emotion.equals("평범")||emotion.equals("모름"))
            return requireContext().getColor(R.color.stat_emotion_56)
        else if(emotion.equals("슬픔")||emotion.equals("우울"))
            return requireContext().getColor(R.color.stat_emotion_78)
        else
            return requireContext().getColor(R.color.stat_emotion_910)
    }
    override fun onDestroyView() {
        super.onDestroyView()
    }
}