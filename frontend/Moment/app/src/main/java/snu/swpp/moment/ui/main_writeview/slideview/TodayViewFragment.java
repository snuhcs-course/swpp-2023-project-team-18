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
import snu.swpp.moment.databinding.TodayItemBinding;
import snu.swpp.moment.exception.NoInternetException;
import snu.swpp.moment.exception.UnauthorizedAccessException;
import snu.swpp.moment.ui.main_writeview.component.BottomButtonContainer;
import snu.swpp.moment.ui.main_writeview.component.ListFooterContainer;
import snu.swpp.moment.ui.main_writeview.uistate.StoryUiState;
import snu.swpp.moment.ui.main_writeview.viewmodel.GetStoryUseCase;
import snu.swpp.moment.ui.main_writeview.viewmodel.SaveScoreUseCase;
import snu.swpp.moment.ui.main_writeview.viewmodel.TodayViewModel;
import snu.swpp.moment.ui.main_writeview.viewmodel.TodayViewModelFactory;
import snu.swpp.moment.utils.KeyboardUtils;
import snu.swpp.moment.utils.TimeConverter;

public class TodayViewFragment extends BaseWritePageFragment {

    private TodayItemBinding binding;
    private List<ListViewItem> listViewItems;
    private ListViewAdapter listViewAdapter;

    private BottomButtonContainer bottomButtonContainer;
    private ListFooterContainer listFooterContainer;

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

        if (viewModel == null) {
            viewModel = new ViewModelProvider(this,
                new TodayViewModelFactory(authenticationRepository, momentRepository,
                    storyRepository, getStoryUseCase, saveScoreUseCase))
                .get(TodayViewModel.class);
        }

        binding = TodayItemBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        listViewItems = new ArrayList<>();

        listViewAdapter = new ListViewAdapter(getContext(), listViewItems);
        binding.todayMomentList.setAdapter(listViewAdapter);
        View footerView = LayoutInflater.from(getContext())
            .inflate(R.layout.listview_footer, binding.todayMomentList, false);
        binding.todayMomentList.addFooterView(footerView);

        // list footer 관리 객체 초기화
        listFooterContainer = new ListFooterContainer(footerView);

        listFooterContainer.setAddButtonOnClickListener(v -> {
            int numMoments = viewModel.getMomentState().getNumMoments();
            if (numMoments >= MOMENT_HOUR_LIMIT) {
                Date createdAt = listViewItems.get(numMoments - MOMENT_HOUR_LIMIT)
                    .getCreatedAt();
                Calendar createdCalendar = Calendar.getInstance();
                createdCalendar.setTime(createdAt);
                int createdHourValue = createdCalendar.get(
                    Calendar.HOUR_OF_DAY); // This will give you the hour of createdSecond

                Calendar currentCalendar = Calendar.getInstance();
                int currentHourValue = currentCalendar.get(
                    Calendar.HOUR_OF_DAY); // This will give you the current hour

                if (createdHourValue == currentHourValue) {
                    listFooterContainer.setUiAddLimitExceeded();
                } else {
                    listFooterContainer.setUiWritingMoment();
                }
            } else {
                listFooterContainer.setUiWritingMoment();
            }
        });

        listFooterContainer.setSubmitButtonOnClickListener(v -> {
            // 소프트 키보드 숨기기
            KeyboardUtils.hideSoftKeyboard(getContext());

            String text = listFooterContainer.getMomentInputText();
            if (!text.isEmpty()) {
                // 새 item 추가
                // 이때 footer의 변화는 아래에서 ListViewAdapter에 등록하는 observer가 처리
                viewModel.writeMoment(text);
                listViewItems.add(new ListViewItem(text, new Date()));
                listViewAdapter.notifyDataSetChanged(true);
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

        listFooterContainer.observeScore(score -> {
            if (score != null) {
                viewModel.saveScore(score);
            }
        });

        // 하단 버튼 관리 객체 초기화
        bottomButtonContainer = new BottomButtonContainer(root, viewModel, listFooterContainer);
        bottomButtonContainer.viewingMoment();

        // 하루 마무리 API 호출 시 동작 설정
        MainActivity activity = (MainActivity) getActivity();
        viewModel.observeCompletionState(activity.completionStateObserver());
        viewModel.observeCompletionState(bottomButtonContainer.completionStateObserver());
        viewModel.observeStoryResultState(bottomButtonContainer.storyResultObserver());
        viewModel.observeEmotionResultState(bottomButtonContainer.emotionResultObserver());
        viewModel.observeTagsResultState(activity.tagsResultObserver());
        viewModel.observeTagsResultState(bottomButtonContainer.tagsResultObserver());
        viewModel.observeAiStoryState(listFooterContainer.aiStoryObserver());

        // AI 답글 대기 중 동작 설정
        listViewAdapter.observeWaitingAiReplySwitch(
            bottomButtonContainer.waitingAiReplySwitchObserver());

        // 마무리 과정 중 뒤로가기 버튼 경고
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d("TodayViewFragment",
                    "handleOnBackPressed: called " + listFooterContainer.isCompletionInProgress());
                if (!listFooterContainer.isCompletionInProgress()) {
                    doOriginalAction();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(),
                    R.style.DialogTheme);
                builder.setMessage(R.string.completion_back_button_popup)
                    .setPositiveButton(R.string.popup_yes, (dialog, id) -> {
                        doOriginalAction();
                    })
                    .setNegativeButton(R.string.popup_no, (dialog, id) -> {
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

        // moment GET API 호출 후 동작
        viewModel.observeMomentState(momentUiState -> {
            Exception error = momentUiState.getError();
            if (error == null) {
                // 모먼트가 하나도 없으면 하단 버튼 비활성화
                int numMoments = momentUiState.getNumMoments();
                bottomButtonContainer.setActivated(numMoments != 0);

                if (numMoments > 0) {
                    listViewItems.clear();
                    for (MomentPairModel momentPair : momentUiState.getMomentPairList()) {
                        listViewItems.add(new ListViewItem(momentPair));
                    }

                    listViewAdapter.notifyDataSetChanged(false);
                    scrollToBottom();
                }
            } else if (error instanceof NoInternetException) {
                Toast.makeText(getContext(), R.string.internet_error, Toast.LENGTH_SHORT)
                    .show();
            } else if (error instanceof UnauthorizedAccessException) {
                Toast.makeText(getContext(), R.string.token_expired_error,
                    Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), LoginRegisterActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_SHORT)
                    .show();
            }

        });

        // story GET API 호출 후 동작
        viewModel.observeSavedStoryState((StoryUiState savedStoryState) -> {
            if (savedStoryState.isEmpty()) {
                return;
            }

            // story가 이미 있으면 마무리된 하루로 판정해 받아온 데이터 보여줌
            Exception error = savedStoryState.getError();
            if (error == null) {
                // SUCCESS
                listFooterContainer.updateUiWithRemoteData(savedStoryState, true);
                bottomButtonContainer.setActivated(false, true);
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

        callApisToRefresh();

        // 날짜 변화 확인해서 GET API 다시 호출
        Runnable refreshRunnable = new Runnable() {
            @Override
            public void run() {
                LocalDateTime now = LocalDateTime.now();
                Log.d("TodayViewFragment", String.format("run: %s", now));

                // 하루가 지났고 하루 마무리 진행 중이 아닐 때
                if (isOutdated() && !listFooterContainer.isCompletionInProgress()) {
                    Log.d("TodayViewFragment", "run: Reloading fragment");
                    callApisToRefresh();
                    updateRefreshTime();
                }
                refreshHandler.postDelayed(this, REFRESH_INTERVAL);
            }
        };
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
        updateRefreshTime();

        KeyboardUtils.hideKeyboardOnOutsideTouch(root, getActivity());

        return root;
    }

    @Override
    protected void callApisToRefresh() {
        LocalDateTime now = LocalDateTime.now();
        viewModel.getMoment(now);
        viewModel.getStory(now);
    }

    @Override
    protected String getDateText() {
        LocalDate today = LocalDate.now();
        return TimeConverter.formatLocalDate(today, "yyyy. MM. dd.");
    }

    private void scrollToBottom() {
        binding.todayMomentList.post(() -> binding.todayMomentList.smoothScrollToPosition(
            binding.todayMomentList.getCount() - 1));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
