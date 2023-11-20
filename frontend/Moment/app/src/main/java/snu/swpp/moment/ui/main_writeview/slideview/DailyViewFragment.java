package snu.swpp.moment.ui.main_writeview.slideview;

import android.content.Context;
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
import snu.swpp.moment.LoginRegisterActivity;
import snu.swpp.moment.R;
import snu.swpp.moment.data.model.MomentPairModel;
import snu.swpp.moment.databinding.PageDailyBinding;
import snu.swpp.moment.ui.main_writeview.component.ListFooterContainer;
import snu.swpp.moment.ui.main_writeview.uistate.MomentUiState;
import snu.swpp.moment.ui.main_writeview.uistate.StoryUiState;
import snu.swpp.moment.ui.main_writeview.viewmodel.DailyViewModel;
import snu.swpp.moment.ui.main_writeview.viewmodel.DailyViewModelFactory;
import snu.swpp.moment.utils.TimeConverter;

public class DailyViewFragment extends BaseWritePageFragment {

    private int minusDays;

    private PageDailyBinding binding;
    private List<ListViewItem> listViewItems;
    private ListViewAdapter listViewAdapter;

    private DailyViewModel viewModel;

    private ListFooterContainer listFooterContainer;

    public static DailyViewFragment initialize(int minusDays) {
        Log.d("DailyViewFragment", "Initializing DailyViewFragment with minusDays: " + minusDays);
        DailyViewFragment fragment = new DailyViewFragment();
        fragment.minusDays = minusDays;
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            viewModel = new ViewModelProvider(this,
                new DailyViewModelFactory(
                    dataUnitFactory.authenticationRepository(),
                    dataUnitFactory.momentRepository(),
                    dataUnitFactory.getStoryUseCase(),
                    dataUnitFactory.saveScoreUseCase()
                )
            ).get(DailyViewModel.class);
        } catch (RuntimeException e) {
            Toast.makeText(requireContext(), "알 수 없는 오류", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireContext(), LoginRegisterActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {

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
        listFooterContainer = new ListFooterContainer(footerView, getViewLifecycleOwner(),
            false);

        // moment & story GET API response를 모두 받았을 때
        apiResponseManager.registerProcessor(((momentUiState, storyUiState) -> {
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

            listFooterContainer.updateWithServerData(storyUiState, false);
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
        listFooterContainer.removeObservers();
    }
}