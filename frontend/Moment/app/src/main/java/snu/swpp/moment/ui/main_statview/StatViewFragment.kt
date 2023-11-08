package snu.swpp.moment.ui.main_statview

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import snu.swpp.moment.data.repository.AuthenticationRepository
import snu.swpp.moment.data.repository.StoryRepository
import snu.swpp.moment.data.source.StoryRemoteDataSource
import snu.swpp.moment.databinding.FragmentStatviewBinding
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale

class StatViewFragment : Fragment() {
    private lateinit var binding: FragmentStatviewBinding
    private lateinit var viewModel: StatViewModel
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
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())


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



        return root
    }
    fun scoreSetup(scores:Map<Int,Int>,today: LocalDate){
        
    }
    fun hashtagSetup(hashtags:Map<String,Int>){

    }
    fun emotionSetup(emotions:Map<String,Int>){

    }
    override fun onDestroyView() {
        super.onDestroyView()
    }
}