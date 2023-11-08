package snu.swpp.moment.ui.main_statview

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
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
        
    }
    fun emotionSetup(emotions:Map<String,Int>){

    }
    override fun onDestroyView() {
        super.onDestroyView()
    }
}