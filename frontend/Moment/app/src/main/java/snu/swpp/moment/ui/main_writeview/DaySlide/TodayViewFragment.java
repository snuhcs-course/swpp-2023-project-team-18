package snu.swpp.moment.ui.main_writeview.DaySlide;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import snu.swpp.moment.LoginRegisterActivity;
import snu.swpp.moment.R;
import snu.swpp.moment.data.model.MomentPairModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;
import snu.swpp.moment.data.repository.StoryRepository;
import snu.swpp.moment.data.source.MomentRemoteDataSource;
import snu.swpp.moment.data.source.StoryRemoteDataSource;
import snu.swpp.moment.databinding.TodayItemBinding;
import snu.swpp.moment.ui.main_writeview.ListViewAdapter;
import snu.swpp.moment.ui.main_writeview.ListViewItem;
import snu.swpp.moment.ui.main_writeview.TodayViewModel;
import snu.swpp.moment.ui.main_writeview.TodayViewModelFactory;
import snu.swpp.moment.ui.main_writeview.uistate.StoryUiState;
import snu.swpp.moment.utils.KeyboardUtils;
import snu.swpp.moment.utils.TimeConverter;

public class TodayViewFragment extends Fragment {

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

    private final int NO_INTERNET = 0;
    private final int ACCESS_TOKEN_EXPIRED = 1;
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

        if (viewModel == null) {
            viewModel = new ViewModelProvider(this,
                new TodayViewModelFactory(authenticationRepository, momentRepository,
                    storyRepository))
                .get(TodayViewModel.class);
        }

        binding = TodayItemBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        listViewItems = new ArrayList<>();

        // moment GET API 호출
        viewModel.observeMomentState(momentUiState -> {
            if (momentUiState.getError() == -1) {
                // 모먼트가 하나도 없으면 하단 버튼 비활성화
                int numMoments = momentUiState.getMomentPairsListSize();
                bottomButtonContainer.setActivated(numMoments != 0);

                if (numMoments > 0) {
                    listViewItems.clear();
                    for (MomentPairModel momentPair : momentUiState.getMomentPairsList()) {
                        listViewItems.add(new ListViewItem(momentPair));
                    }

                    listViewAdapter.notifyDataSetChanged();
                    scrollToBottom();
                }
            } else {
                if (momentUiState.getError() == NO_INTERNET) {
                    Toast.makeText(getContext(), R.string.internet_error, Toast.LENGTH_SHORT)
                        .show();
                } else if (momentUiState.getError() == ACCESS_TOKEN_EXPIRED) {
                    Toast.makeText(getContext(), R.string.token_expired_error,
                        Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getContext(), LoginRegisterActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_SHORT)
                        .show();
                }
            }
        });
        viewModel.getMoment(LocalDate.now());

        // story GET API 호출
        viewModel.observeSavedStoryState((StoryUiState savedStoryState) -> {
            // TODO: story 없으면 moment add 버튼 보여주고, 있으면 그 내용 보여주기
        });
        viewModel.getStory(LocalDate.now());

        listViewAdapter = new ListViewAdapter(getContext(), listViewItems);
        binding.todayMomentList.setAdapter(listViewAdapter);
        View footerView = LayoutInflater.from(getContext())
            .inflate(R.layout.listview_footer, binding.todayMomentList, false);
        binding.todayMomentList.addFooterView(footerView);

        // list footer 관리 객체 초기화
        listFooterContainer = new ListFooterContainer(footerView);

        listFooterContainer.setAddButtonOnClickListener(v -> {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm",
                Locale.getDefault());

            int numMoments = viewModel.getMomentState().getMomentPairsListSize();
            if (numMoments >= MOMENT_HOUR_LIMIT) {
                String createdSecond = listViewItems.get(numMoments - MOMENT_HOUR_LIMIT)
                    .getInputTime();

                try {
                    Date createdDate = inputFormat.parse(createdSecond);
                    Calendar createdCalendar = Calendar.getInstance();
                    createdCalendar.setTime(createdDate);
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
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            } else {
                listFooterContainer.setUiWritingMoment();
            }
        });

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
            INPUT_METHOD_SERVICE);
        listFooterContainer.setSubmitButtonOnClickListener(v -> {
            String text = listFooterContainer.getMomentInputText();

            // 소프트 키보드 숨기기
            if (imm != null && getActivity().getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            }

            if (!text.isEmpty()) {
                viewModel.writeMoment(text);
                addItem(text);
                listFooterContainer.setUiReadyToAddMoment();
            }
        });

        listFooterContainer.observeScrollToBottomSwitch(isSet -> {
            if (isSet) {
                scrollToBottom();
            }
        });

        listFooterContainer.observeAiStoryCallSwitch(isSet -> {
            if (isSet) {
                viewModel.getAiStory();
            }
        });

        // 하단 버튼 관리 객체 초기화
        bottomButtonContainer = new BottomButtonContainer(root, listFooterContainer);
        bottomButtonContainer.viewingMoment();

        // 하루 마무리 API 호출 시 동작 설정
        viewModel.observeCompletionState(bottomButtonContainer.completionStateObserver());
        viewModel.observeStoryResultState(bottomButtonContainer.storyResultObserver());
        viewModel.observeEmotionResultState(bottomButtonContainer.emotionResultObserver());
        viewModel.observeTagsResultState(bottomButtonContainer.tagsResultObserver());
        viewModel.observeAiStoryState(listFooterContainer.aiStoryObserver());

        KeyboardUtils.hideKeyboardOnOutsideTouch(root, getActivity());

        return root;
    }

    private void addItem(String userInput) {
        String currentTime = TimeConverter.formatDate(new Date(), "yyyy.MM.dd. HH:mm");
        listViewItems.add(new ListViewItem(userInput, currentTime, ""));
        listViewAdapter.notifyDataSetChanged();
        scrollToBottom();
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
