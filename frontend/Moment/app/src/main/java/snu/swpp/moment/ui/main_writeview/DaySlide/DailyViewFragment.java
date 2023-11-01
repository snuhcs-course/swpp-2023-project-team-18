package snu.swpp.moment.ui.main_writeview.DaySlide;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import snu.swpp.moment.LoginRegisterActivity;
import snu.swpp.moment.R;
import snu.swpp.moment.data.model.MomentPairModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;
import snu.swpp.moment.data.repository.StoryRepository;
import snu.swpp.moment.data.source.MomentRemoteDataSource;
import snu.swpp.moment.data.source.StoryRemoteDataSource;
import snu.swpp.moment.databinding.DailyItemBinding;
import snu.swpp.moment.exception.NoInternetException;
import snu.swpp.moment.exception.UnauthorizedAccessException;
import snu.swpp.moment.ui.main_writeview.DailyViewModel;
import snu.swpp.moment.ui.main_writeview.DailyViewModelFactory;
import snu.swpp.moment.ui.main_writeview.GetStoryUseCase;
import snu.swpp.moment.ui.main_writeview.ListViewAdapter;
import snu.swpp.moment.ui.main_writeview.ListViewItem;
import snu.swpp.moment.ui.main_writeview.SaveScoreUseCase;
import snu.swpp.moment.ui.main_writeview.uistate.MomentUiState;
import snu.swpp.moment.ui.main_writeview.uistate.StoryUiState;
import snu.swpp.moment.utils.TimeConverter;

public class DailyViewFragment extends Fragment {

    private int minusDays;

    private DailyItemBinding binding;
    private List<ListViewItem> listViewItems;
    private ListViewAdapter listViewAdapter;

    private DailyViewModel viewModel;
    private MomentRemoteDataSource momentRemoteDataSource;
    private StoryRemoteDataSource storyRemoteDataSource;
    private MomentRepository momentRepository;
    private StoryRepository storyRepository;
    private GetStoryUseCase getStoryUseCase;
    private SaveScoreUseCase saveScoreUseCase;
    private AuthenticationRepository authenticationRepository;

    private ListFooterContainer listFooterContainer;

    public static DailyViewFragment initialize(int minusDays) {
        Log.d("DailyViewFragment", "Initializing DailyViewFragment with minusDays: " + minusDays);
        DailyViewFragment fragment = new DailyViewFragment();
        fragment.minusDays = minusDays;
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {

        momentRemoteDataSource = Objects.requireNonNullElse(momentRemoteDataSource,
            new MomentRemoteDataSource());
        storyRemoteDataSource = Objects.requireNonNullElse(storyRemoteDataSource,
            new StoryRemoteDataSource());
        momentRepository = Objects.requireNonNullElse(momentRepository,
            new MomentRepository(momentRemoteDataSource));
        storyRepository = Objects.requireNonNullElse(storyRepository,
            new StoryRepository(storyRemoteDataSource));

        try {
            authenticationRepository = AuthenticationRepository.getInstance(getContext());
        } catch (Exception e) {
            Toast.makeText(getContext(), "알 수 없는 인증 오류", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), LoginRegisterActivity.class);
            startActivity(intent);
        }

        getStoryUseCase = Objects.requireNonNullElse(getStoryUseCase,
            new GetStoryUseCase(authenticationRepository, storyRepository));
        saveScoreUseCase = Objects.requireNonNullElse(saveScoreUseCase,
            new SaveScoreUseCase(authenticationRepository, storyRepository));

        viewModel = Objects.requireNonNullElse(viewModel,
            new ViewModelProvider(this, new DailyViewModelFactory(
                authenticationRepository,
                momentRepository,
                getStoryUseCase,
                saveScoreUseCase)).get(
                DailyViewModel.class));

        binding = DailyItemBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        listViewItems = new ArrayList<>();

        viewModel.observeMomentState((MomentUiState momentUiState) -> {
            Exception error = momentUiState.getError();
            if (error == null) {
                // SUCCESS
                int numMoments = momentUiState.getNumMoments();
                if (numMoments > 0) {
                    listViewItems.clear();
                    for (MomentPairModel momentPair : momentUiState.getMomentPairList()) {
                        listViewItems.add(new ListViewItem(momentPair));
                    }

                    listViewAdapter.notifyDataSetChanged();
                } else {
                    binding.noMomentText.setVisibility(View.VISIBLE);
                }
            } else if (error instanceof NoInternetException) {
                // NO INTERNET
                Toast.makeText(getContext(), R.string.internet_error, Toast.LENGTH_SHORT)
                    .show();
            } else if (error instanceof UnauthorizedAccessException) {
                // ACCESS TOKEN EXPIRED
                Toast.makeText(getContext(), R.string.token_expired_error,
                    Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), LoginRegisterActivity.class);
                startActivity(intent);
            } else {
                Log.d("DailyViewFragment", "Unknown error: " + error.getMessage());
                Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_SHORT)
                    .show();
            }
        });

        listViewAdapter = new ListViewAdapter(getContext(), listViewItems);
        binding.dailyMomentList.setAdapter(listViewAdapter);
        View footerView = LayoutInflater.from(getContext())
            .inflate(R.layout.listview_footer, null, false);
        binding.dailyMomentList.addFooterView(footerView);

        // list footer 관리 객체 초기화
        listFooterContainer = new ListFooterContainer(footerView);

        listFooterContainer.observeScore(score -> {
            viewModel.saveScore(score);
        });

        viewModel.observeStoryState((StoryUiState storyUiState) -> {
            Exception error = storyUiState.getError();
            if (error == null) {
                // SUCCESS
                listFooterContainer.updateUiWithRemoteData(storyUiState);
            } else if (error instanceof NoInternetException) {
                // NO INTERNET
                Toast.makeText(getContext(), R.string.internet_error, Toast.LENGTH_SHORT)
                    .show();
            } else if (error instanceof UnauthorizedAccessException) {
                // ACCESS TOKEN EXPIRED
                Toast.makeText(getContext(), R.string.token_expired_error,
                    Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), LoginRegisterActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_SHORT)
                    .show();
            }
        });

        LocalDate date = TimeConverter.getToday().minusDays(minusDays);
        LocalDateTime dateTime = date.atTime(3, 0);
        viewModel.getMoment(dateTime);
        viewModel.getStory(dateTime);

        Log.d("DailyViewFragment", "onCreateView() ended");

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}