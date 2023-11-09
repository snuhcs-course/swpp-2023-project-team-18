package snu.swpp.moment.ui.main_writeview.slideview;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
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
import snu.swpp.moment.databinding.PageDailyBinding;
import snu.swpp.moment.exception.NoInternetException;
import snu.swpp.moment.exception.UnauthorizedAccessException;
import snu.swpp.moment.ui.main_writeview.component.ListFooterContainer;
import snu.swpp.moment.ui.main_writeview.uistate.MomentUiState;
import snu.swpp.moment.ui.main_writeview.uistate.StoryUiState;
import snu.swpp.moment.ui.main_writeview.viewmodel.DailyViewModel;
import snu.swpp.moment.ui.main_writeview.viewmodel.DailyViewModelFactory;
import snu.swpp.moment.ui.main_writeview.viewmodel.GetStoryUseCase;
import snu.swpp.moment.ui.main_writeview.viewmodel.SaveScoreUseCase;
import snu.swpp.moment.utils.TimeConverter;

public class DailyViewFragment extends BaseWritePageFragment {

    private int minusDays;

    private PageDailyBinding binding;
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
            authenticationRepository = AuthenticationRepository.getInstance(requireContext());
        } catch (Exception e) {
            Toast.makeText(requireContext(), "알 수 없는 인증 오류", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireContext(), LoginRegisterActivity.class);
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

        binding = PageDailyBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // ListView setup
        listViewItems = new ArrayList<>();
        listViewAdapter = new ListViewAdapter(requireContext(), listViewItems);
        listViewAdapter.setAnimation(false);

        binding.dailyMomentList.setAdapter(listViewAdapter);
        View footerView = LayoutInflater.from(requireContext())
            .inflate(R.layout.listview_footer, binding.dailyMomentList, false);
        binding.dailyMomentList.addFooterView(footerView);

        callApisToRefresh();

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
                Toast.makeText(requireContext(), R.string.internet_error, Toast.LENGTH_SHORT)
                    .show();
            } else if (error instanceof UnauthorizedAccessException) {
                // ACCESS TOKEN EXPIRED
                Toast.makeText(requireContext(), R.string.token_expired_error,
                    Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(requireContext(), LoginRegisterActivity.class);
                startActivity(intent);
            } else {
                Log.d("DailyViewFragment", "Unknown error: " + error.getMessage());
                Toast.makeText(requireContext(), R.string.unknown_error, Toast.LENGTH_SHORT)
                    .show();
            }
        });

        // list footer 관리 객체 초기화
        listFooterContainer = new ListFooterContainer(footerView);

        listFooterContainer.observeSaveScoreSwitch(saveScoreSwitch -> {
            if (saveScoreSwitch) {
                viewModel.saveScore(listFooterContainer.getScore());
            }
        });

        viewModel.observeStoryState((StoryUiState storyUiState) -> {
            Exception error = storyUiState.getError();
            if (error == null) {
                // SUCCESS
                listFooterContainer.updateUiWithRemoteData(storyUiState, false);
                if (!storyUiState.isEmpty()) {
                    binding.dailyMomentList.post(() -> binding.dailyMomentList.setSelection(
                        binding.dailyMomentList.getCount() - 1));
                }
            } else if (error instanceof NoInternetException) {
                // NO INTERNET
                Toast.makeText(requireContext(), R.string.internet_error, Toast.LENGTH_SHORT)
                    .show();
            } else if (error instanceof UnauthorizedAccessException) {
                // ACCESS TOKEN EXPIRED
                Toast.makeText(requireContext(), R.string.token_expired_error,
                    Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(requireContext(), LoginRegisterActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), R.string.unknown_error, Toast.LENGTH_SHORT)
                    .show();
            }
        });

        // 날짜 변화 확인해서 GET API 다시 호출
        Runnable refreshRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d("DailyViewFragment", "refreshRunnable running; currentDateTime: "
                    + getCurrentDateTime());
                Log.d("DailyViewFragment",
                    "refreshRunnable: current lastRefreshedTime: " + lastRefreshedTime);

                // 하루가 지났을 때
                if (isOutdated()) {
                    Log.d("DailyViewFragment", "refreshRunnable: Outdated, call APIs to refresh");
                    setToolbarTitle();
                    callApisToRefresh();
                    updateRefreshTime();
                }
                registerRefreshRunnable(this);
            }
        };
        registerRefreshRunnable(refreshRunnable);
        updateRefreshTime();

        Log.d("DailyViewFragment", "onCreateView() ended");
        return root;
    }

    @Override
    protected void callApisToRefresh() {
        LocalDateTime currentDateTime = getCurrentDateTime();
        viewModel.getMoment(currentDateTime);
        viewModel.getStory(currentDateTime);
    }

    @Override
    protected LocalDate getDate() {
        return TimeConverter.getToday().minusDays(minusDays);
    }

    private LocalDateTime getCurrentDateTime() {
        return getDate().atTime(3, 0, 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}