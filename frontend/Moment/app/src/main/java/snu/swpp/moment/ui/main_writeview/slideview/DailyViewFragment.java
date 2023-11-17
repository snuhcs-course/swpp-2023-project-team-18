package snu.swpp.moment.ui.main_writeview.slideview;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
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
import snu.swpp.moment.ui.main_writeview.component.ListFooterContainerNew;
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

    private ListFooterContainerNew listFooterContainer;

    public static DailyViewFragment initialize(int minusDays) {
        Log.d("DailyViewFragment", "Initializing DailyViewFragment with minusDays: " + minusDays);
        DailyViewFragment fragment = new DailyViewFragment();
        fragment.minusDays = minusDays;
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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

        // list footer 관리 객체 초기화
        listFooterContainer = new ListFooterContainerNew(footerView, getViewLifecycleOwner(),
            false);

        // moment & story GET API response를 모두 받았을 때
        apiResponseManager.registerProcessor(((momentUiState, storyUiState) -> {
            // moment GET API 호출 후 동작
            listViewItems.clear();
            if (momentUiState.getNumMoments() > 0) {
                binding.noMomentText.setVisibility(View.GONE);
                for (MomentPairModel momentPair : momentUiState.getMomentPairList()) {
                    listViewItems.add(new ListViewItem(momentPair));
                }
            } else {
                binding.noMomentText.setVisibility(View.VISIBLE);
            }
            listViewAdapter.notifyDataSetChanged();

            // story GET API 호출 후 동작
            listFooterContainer.updateWithServerData(storyUiState, false);
            if (!storyUiState.hasNoData()) {
                binding.dailyMomentList.post(() -> binding.dailyMomentList.setSelection(
                    binding.dailyMomentList.getCount() - 1));
            }
        }));

        // moment GET API response를 받았을 때
        viewModel.observeMomentState((MomentUiState momentUiState) -> {
            Exception error = momentUiState.getError();
            if (error != null) {
                handleApiError(error);
                return;
            }
            apiResponseManager.saveResponse(momentUiState);
            apiResponseManager.process();
        });

        // story GET API response를 받았을 때
        viewModel.observeStoryState((StoryUiState storyUiState) -> {
            Exception error = storyUiState.getError();
            if (error != null) {
                handleApiError(error);
                return;
            }
            apiResponseManager.saveResponse(storyUiState);
            apiResponseManager.process();
        });

        Log.d("DailyViewFragment", "onCreateView: initial API call to refresh");
        callApisToRefresh();
        updateRefreshTime();

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

        Log.d("DailyViewFragment", "onCreateView() ended");
        return root;
    }

    @Override
    protected void callApisToRefresh() {
        LocalDateTime currentDateTime = getCurrentDateTime();
        Log.d("DailyViewFragment", "callApisToRefresh: called with timestamp " + currentDateTime);
        apiResponseManager.reset();
        viewModel.getMoment(currentDateTime);
        viewModel.getStory(currentDateTime);
    }

    @Override
    protected LocalDate getCurrentDate() {
        return TimeConverter.getToday().minusDays(minusDays);
    }

    @Override
    protected LocalDateTime getCurrentDateTime() {
        return getCurrentDate().atTime(3, 0, 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}