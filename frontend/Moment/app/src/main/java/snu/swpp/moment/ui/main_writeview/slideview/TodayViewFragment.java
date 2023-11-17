package snu.swpp.moment.ui.main_writeview.slideview;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import snu.swpp.moment.LoginRegisterActivity;
import snu.swpp.moment.MainActivity;
import snu.swpp.moment.R;
import snu.swpp.moment.data.model.MomentPairModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;
import snu.swpp.moment.data.repository.StoryRepository;
import snu.swpp.moment.data.source.MomentRemoteDataSource;
import snu.swpp.moment.data.source.StoryRemoteDataSource;
import snu.swpp.moment.databinding.PageTodayBinding;
import snu.swpp.moment.ui.main_writeview.component.BottomButtonContainer;
import snu.swpp.moment.ui.main_writeview.component.ListFooterContainer;
import snu.swpp.moment.ui.main_writeview.component.NudgeHeaderContainer;
import snu.swpp.moment.ui.main_writeview.component.WritePageState;
import snu.swpp.moment.ui.main_writeview.uistate.NudgeUiState;
import snu.swpp.moment.ui.main_writeview.uistate.StoryUiState;
import snu.swpp.moment.ui.main_writeview.viewmodel.GetStoryUseCase;
import snu.swpp.moment.ui.main_writeview.viewmodel.SaveScoreUseCase;
import snu.swpp.moment.ui.main_writeview.viewmodel.TodayViewModel;
import snu.swpp.moment.ui.main_writeview.viewmodel.TodayViewModelFactory;
import snu.swpp.moment.utils.KeyboardUtils;
import snu.swpp.moment.utils.TimeConverter;

public class TodayViewFragment extends BaseWritePageFragment {

    private PageTodayBinding binding;
    private List<ListViewItem> listViewItems;
    private ListViewAdapter listViewAdapter;

    private BottomButtonContainer bottomButtonContainer;
    private ListFooterContainer listFooterContainer;
    private NudgeHeaderContainer nudgeHeaderContainer;

    private TodayViewModel viewModel;

    private AuthenticationRepository authenticationRepository;
    private MomentRepository momentRepository;
    private MomentRemoteDataSource momentRemoteDataSource;
    private StoryRepository storyRepository;
    private StoryRemoteDataSource storyRemoteDataSource;
    private GetStoryUseCase getStoryUseCase;
    private SaveScoreUseCase saveScoreUseCase;

    private final int MOMENT_HOUR_LIMIT = 2;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        momentRemoteDataSource = Objects.requireNonNullElse(momentRemoteDataSource,
            new MomentRemoteDataSource());
        momentRepository = Objects.requireNonNullElse(momentRepository,
            new MomentRepository(momentRemoteDataSource));
        storyRemoteDataSource = Objects.requireNonNullElse(storyRemoteDataSource,
            new StoryRemoteDataSource());
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

        if (viewModel == null) {
            viewModel = new ViewModelProvider(this,
                new TodayViewModelFactory(authenticationRepository, momentRepository,
                    storyRepository, getStoryUseCase, saveScoreUseCase)).get(TodayViewModel.class);
        }

        binding = PageTodayBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // ListView setup
        listViewItems = new ArrayList<>();
        listViewAdapter = new ListViewAdapter(requireContext(), listViewItems);
        binding.todayMomentList.setAdapter(listViewAdapter);

        // list header & footer 등록
        View headerView = LayoutInflater.from(requireContext())
            .inflate(R.layout.nudge_header, binding.todayMomentList, false);
        binding.todayMomentList.addHeaderView(headerView);
        View footerView = LayoutInflater.from(requireContext())
            .inflate(R.layout.listview_footer, binding.todayMomentList, false);
        binding.todayMomentList.addFooterView(footerView);

        // list footer 관리 객체 초기화
        listFooterContainer = new ListFooterContainer(footerView, getViewLifecycleOwner(), true);

        listFooterContainer.setAddButtonOnClickListener(v -> {
            int numMoments = viewModel.getMomentState().getNumMoments();
            if (numMoments >= MOMENT_HOUR_LIMIT) {
                Date createdAt = listViewItems.get(numMoments - MOMENT_HOUR_LIMIT).getCreatedAt();
                Calendar createdCalendar = Calendar.getInstance();
                createdCalendar.setTime(createdAt);
                int createdHourValue = createdCalendar.get(
                    Calendar.HOUR_OF_DAY); // This will give you the hour of createdSecond

                Calendar currentCalendar = Calendar.getInstance();
                int currentHourValue = currentCalendar.get(
                    Calendar.HOUR_OF_DAY); // This will give you the current hour

                if (createdHourValue == currentHourValue) {
                    listFooterContainer.setState(WritePageState.MOMENT_ADD_LIMIT_EXCEEDED);
                } else {
                    listFooterContainer.setState(WritePageState.MOMENT_WRITING);
                }
            } else {
                listFooterContainer.setState(WritePageState.MOMENT_WRITING);
            }
        });

        listFooterContainer.setSubmitButtonOnClickListener(v -> {
            // 소프트 키보드 숨기기
            KeyboardUtils.hideSoftKeyboard(requireContext());

            String text = listFooterContainer.getMomentInput();
            if (!text.isEmpty()) {
                // 새 item 추가
                // 이때 footer의 변화는 아래에서 ListViewAdapter에 등록하는 observer가 처리
                viewModel.writeMoment(text);
                listViewAdapter.setAnimation(true);
                listViewItems.add(new ListViewItem(text, new Date()));
                listViewAdapter.notifyDataSetChanged();
                scrollToBottom();
            }
        });

        listFooterContainer.observeScrollToBottomSwitch(isSet -> {
            if (isSet) {
                Log.d("TodayViewFragment", "scrollToBottom switch set");
                scrollToBottom();
            }
        });
        listFooterContainer.observeAiStoryCallSwitch(isSet -> {
            if (isSet) {
                viewModel.getAiStory();
            }
        });

        // nudge header 관리 객체 초기화
        nudgeHeaderContainer = new NudgeHeaderContainer(headerView);
        // TODO: API로 받아온 UiState를 observe 해서 내용 update
        final String nudgeContent = "요즘은 계속 우울한 나날을 보내고 계신 것 같아요. 오늘은 기분이 어때요? 어떤 재미있는 계획이 있나요?";
        nudgeHeaderContainer.updateUi(new NudgeUiState(null, false, nudgeContent));

        nudgeHeaderContainer.observeDeleteSwitch(deleteSwitch -> {
            if (deleteSwitch) {
                // TODO: nudge 숨기기 API 호출
            }
        });

        // 하단 버튼 관리 객체 초기화
        bottomButtonContainer = new BottomButtonContainer(root, viewModel, listFooterContainer);

        // 하루 마무리 API 호출 시 동작 설정
        MainActivity activity = (MainActivity) requireActivity();
        viewModel.observeCompletionState(activity.completionStateObserver());
        viewModel.observeCompletionState(bottomButtonContainer.completionStateObserver());
        viewModel.observeStoryResultState(bottomButtonContainer.storyResultObserver());
        viewModel.observeEmotionResultState(bottomButtonContainer.emotionResultObserver());
        viewModel.observeTagsResultState(activity.tagsResultObserver());
        viewModel.observeTagsResultState(bottomButtonContainer.tagsResultObserver());
        viewModel.observeScoreResultState(bottomButtonContainer.scoreResultObserver());
        viewModel.observeAiStoryState(listFooterContainer.aiStoryObserver());

        // AI 답글 대기 중 동작 설정
        listViewAdapter.observeWaitingAiReplySwitch(
            bottomButtonContainer.waitingAiReplySwitchObserver());

        // 마무리 과정 중 뒤로가기 버튼 경고
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d("TodayViewFragment", "handleOnBackPressed: called / isCompletionInProgress="
                    + listFooterContainer.isCompletionInProgress());
                if (!listFooterContainer.isCompletionInProgress()) {
                    doOriginalAction();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(),
                    R.style.DialogTheme);
                builder.setMessage(R.string.completion_back_button_popup)
                    .setPositiveButton(R.string.popup_yes, (dialog, id) -> {
                        doOriginalAction();
                    }).setNegativeButton(R.string.popup_no, (dialog, id) -> {
                    });
                builder.create().show();
            }

            private void doOriginalAction() {
                setEnabled(false);
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        };
        requireActivity().getOnBackPressedDispatcher()
            .addCallback(getViewLifecycleOwner(), onBackPressedCallback);

        // moment & story GET API response를 모두 받았을 때
        apiResponseManager.registerProcessor(((momentUiState, storyUiState) -> {
            // moment state
            listViewItems.clear();
            listViewAdapter.setAnimation(false);

            boolean hasMoment = momentUiState.getNumMoments() > 0;
            if (hasMoment) {
                Log.d("TodayViewFragment", "Got moment GET response: numMoments="
                    + momentUiState.getNumMoments());
            }
            for (MomentPairModel momentPair : momentUiState.getMomentPairList()) {
                listViewItems.add(new ListViewItem(momentPair));
            }
            listViewAdapter.notifyDataSetChanged();

            // story state
            listFooterContainer.updateWithServerData(storyUiState, true);
            if (storyUiState.hasNoData()) {
                Log.d("TodayViewFragment", "Got story GET response: story has no data");
                bottomButtonContainer.setState(WritePageState.MOMENT_READY_TO_ADD);
                bottomButtonContainer.setActivated(hasMoment);
            } else {
                Log.d("TodayViewFragment",
                    "Got story GET response: story has valid data");
                bottomButtonContainer.setState(WritePageState.COMPLETE);
                scrollToBottom();
            }
        }));

        // moment GET API response를 받았을 때
        viewModel.observeMomentState(momentUiState -> {
            Exception error = momentUiState.getError();
            Log.d("TodayViewFragment", "Got moment GET response: error=" + error);
            if (error != null) {
                handleApiError(error);
                return;
            }
            apiResponseManager.saveResponse(momentUiState);
            apiResponseManager.process();
        });

        // story GET API response를 받았을 때
        viewModel.observeSavedStoryState((StoryUiState savedStoryState) -> {
            Exception error = savedStoryState.getError();
            Log.d("TodayViewFragment", "Got story GET response: error=" + error + ", isEmpty="
                + savedStoryState.isEmpty());
            if (error != null) {
                handleApiError(error);
                return;
            }
            apiResponseManager.saveResponse(savedStoryState);
            apiResponseManager.process();
        });

        Log.d("TodayViewFragment", "onCreateView: initial API call to refresh");
        callApisToRefresh();
        updateRefreshTime();

        // 날짜 변화 확인해서 GET API 다시 호출
        Runnable refreshRunnable = new Runnable() {
            @Override
            public void run() {
                LocalDateTime now = getCurrentDateTime();
                Log.d("TodayViewFragment", "refreshRunnable running at " + now);
                Log.d("TodayViewFragment",
                    "refreshRunnable: current lastRefreshedTime: " + lastRefreshedTime);

                // 하루가 지났고 하루 마무리 진행 중이 아닐 때
                if (isOutdated() && !listFooterContainer.isCompletionInProgress()) {
                    Log.d("TodayViewFragment", "refreshRunnable: Outdated, call APIs to refresh");
                    setToolbarTitle();
                    callApisToRefresh();
                    updateRefreshTime();
                }
                registerRefreshRunnable(this);
            }
        };
        registerRefreshRunnable(refreshRunnable);

        KeyboardUtils.hideKeyboardOnOutsideTouch(root, requireActivity());

        return root;
    }

    @Override
    protected void callApisToRefresh() {
        LocalDateTime now = getCurrentDateTime();
        Log.d("TodayViewFragment", "callApisToRefresh: called with timestamp " + now);
        apiResponseManager.reset();
        viewModel.getMoment(now);
        viewModel.getStory(now);
    }

    @Override
    protected LocalDate getCurrentDate() {
        return TimeConverter.getToday();
    }

    @Override
    protected LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    private void scrollToBottom() {
        if (binding == null) {
            Log.d("TodayViewFragment", "scrollToBottom: binding is null");
            return;
        }
        binding.todayMomentList.post(() -> binding.todayMomentList.smoothScrollToPosition(
            binding.todayMomentList.getCount() - 1));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        listFooterContainer.removeObservers();
    }
}
