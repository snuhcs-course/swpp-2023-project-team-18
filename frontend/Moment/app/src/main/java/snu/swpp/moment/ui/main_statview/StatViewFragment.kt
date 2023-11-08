package snu.swpp.moment.ui.main_statview

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import snu.swpp.moment.data.repository.AuthenticationRepository
import snu.swpp.moment.data.repository.StoryRepository
import snu.swpp.moment.data.source.StoryRemoteDataSource
import snu.swpp.moment.databinding.FragmentStatviewBinding
import snu.swpp.moment.ui.main_monthview.MonthViewModel
import snu.swpp.moment.ui.main_monthview.MonthViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale

class StatViewFragment : Fragment() {
    private var binding: FragmentStatviewBinding? = null
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
        val root: View = binding!!.root
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val textView = binding!!.statview
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}