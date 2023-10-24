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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import snu.swpp.moment.LoginRegisterActivity;
import snu.swpp.moment.R;
import snu.swpp.moment.data.model.MomentPair;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;
import snu.swpp.moment.data.source.MomentRemoteDataSource;
import snu.swpp.moment.databinding.TodayItemBinding;
import snu.swpp.moment.ui.main_writeview.ListViewAdapter;
import snu.swpp.moment.ui.main_writeview.ListViewItem;
import snu.swpp.moment.ui.main_writeview.MomentUiState;
import snu.swpp.moment.ui.main_writeview.WriteViewModel;
import snu.swpp.moment.ui.main_writeview.WriteViewModelFactory;
import snu.swpp.moment.utils.KeyboardUtils;

public class TodayViewFragment extends Fragment {

    private TodayItemBinding binding;
    private List<ListViewItem> items;
    private ListViewAdapter mAdapter;

    private BottomButtonContainer bottomButtonContainer;
    private ListFooterContainer listFooterContainer;

    private WriteViewModel viewModel;
    private MomentRemoteDataSource remoteDataSource;
    private MomentRepository momentRepository;
    private AuthenticationRepository authenticationRepository;

    private int numMoments;

    private final int NO_INTERNET = 0;
    private final int ACCESS_TOKEN_EXPIRED = 1;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        if (remoteDataSource == null) {
            remoteDataSource = new MomentRemoteDataSource();
        }
        if (momentRepository == null) {
            momentRepository = new MomentRepository(remoteDataSource);
        }
        try {
            authenticationRepository = AuthenticationRepository.getInstance(getContext());
        } catch (Exception e) {
            Toast.makeText(getContext(), "알 수 없는 인증 오류", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), LoginRegisterActivity.class);
            startActivity(intent);
        }
        if (viewModel == null) {
            viewModel = new ViewModelProvider(this,
                new WriteViewModelFactory(authenticationRepository, momentRepository))
                .get(WriteViewModel.class);
        }

        binding = TodayItemBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize ListView and related components
        initializeListView(root);
        KeyboardUtils.hideKeyboardOnOutsideTouch(root, getActivity());

        // 하단 버튼 관리 객체 초기화
        bottomButtonContainer = new BottomButtonContainer(root, listFooterContainer);
        bottomButtonContainer.viewingMoment();

        // 루트 뷰에 터치 리스너 설정

        return root;
    }

    private void initializeListView(View root) {
        items = new ArrayList<>();
        System.out.println("#Debug items size" + items.size());

        viewModel.getMomentState().observe(getViewLifecycleOwner(), new Observer<MomentUiState>() {
            @Override
            public void onChanged(MomentUiState momentUiState) {
                System.out.println("#DEBUG: ON CHANGED RUN");
                if (momentUiState.getError() == -1) {
                    if (!momentUiState.getMomentPairsList().isEmpty()) {
                        items.clear();
                        numMoments = momentUiState.getMomentPairsList().size();
                        for (MomentPair momentPair : momentUiState.getMomentPairsList()) {
                            String userInput = momentPair.getMoment();
                            String serverResponse = momentPair.getReply();
                            String createdTime = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(
                                momentPair.getMomentCreatedTime());
                            items.add(new ListViewItem(userInput, createdTime, serverResponse));
                        }

                        bottomButtonContainer.setActivated(numMoments != 0);
                        // TODO: 버튼 활성화 조건 점검 -- 이 부분은 언제 진입하게 되는가?

                        mAdapter.notifyDataSetChanged();
                        binding.listviewList.setSelection(items.size() - 1);
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
            }
        });

        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        int date = today.getDayOfMonth();
        viewModel.getMoment(year, month, date);

        mAdapter = new ListViewAdapter(getContext(), items);
        binding.listviewList.setAdapter(mAdapter);
        View footerView = LayoutInflater.from(getContext())
            .inflate(R.layout.listview_footer, binding.listviewList, false);
        binding.listviewList.addFooterView(footerView);

        // list footer 관리 객체 초기화
        listFooterContainer = new ListFooterContainer(footerView);

        listFooterContainer.setAddButtonOnClickListener(v -> {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm",
                Locale.getDefault());
            if (numMoments >= 2) {
                String createdSecond = items.get(numMoments - 2).getInputTime();

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
                        listFooterContainer.setUiLimitExceeded();
                    } else {
                        listFooterContainer.setUiWritingMoment();
                        binding.listviewList.setSelection(items.size() - 1);
                        // ScrollView를 ConstraintLayout의 하단으로 스크롤
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            } else {
                listFooterContainer.setUiWritingMoment();
                // ScrollView를 ConstraintLayout의 하단으로 스크롤
                binding.listviewList.setSelection(items.size() - 1);
            }
        });

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
            INPUT_METHOD_SERVICE);
        listFooterContainer.setSubmitButtonOnClickListener(v -> {
            String text = listFooterContainer.getInputText();

            // 소프트 키보드 숨기기
            if (imm != null && getActivity().getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            }

            if (!text.isEmpty()) {
                // FIXME: commented out for test (no GPT API call)
                // viewModel.writeMoment(text);
                addItem(text);
                listFooterContainer.setUiReadyToAdd();
            }
        });
    }

    private void addItem(String userInput) {
        String currentTime = new SimpleDateFormat("yyyy.MM.dd. HH:mm").format(new Date());
        items.add(new ListViewItem(userInput, currentTime, ""));
        mAdapter.notifyDataSetChanged();
        binding.listviewList.setSelection(items.size() - 1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
