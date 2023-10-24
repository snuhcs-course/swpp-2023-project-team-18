package snu.swpp.moment.ui.main_writeview.DaySlide;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
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
    private Button addButton, addButtonInactivate, submitButton, submitButtonInactivate, dayCompletionButton;
    private EditText inputEditText;
    private TextView textCount, addButtonText, addButtonInactivateText;
    private ScrollView scrollView;

    private ConstraintLayout constraintLayout;

    private int MAX_LENGTH = 1000;
    private final int NO_INTERNET = 0;
    private final int ACCESS_TOKEN_EXPIRED = 1;
    private WriteViewModel viewModel;
    private MomentRemoteDataSource remoteDataSource;
    private MomentRepository momentRepository;
    private AuthenticationRepository authenticationRepository;

    private int numMoments;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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
            Toast.makeText(getContext(), "알 수 없는 인증 오류", Toast.LENGTH_SHORT);
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

        System.out.println("#DEBUG: before initializeListView");
        // Initialize ListView and related components
        initializeListView(root);
        System.out.println("#DEBUG: after initializeListView");
        KeyboardUtils.hideKeyboardOnOutsideTouch(root, getActivity());

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
                        //System.out.println("#DEBUG: ON CHANGED RUN 2");
                        items.clear();
                        numMoments = momentUiState.getMomentPairsList().size();
                        for (MomentPair momentPair : momentUiState.getMomentPairsList()) {
                            String userInput = momentPair.getMoment();
                            String serverResponse = momentPair.getReply();
                            String createdTime = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(
                                momentPair.getMomentCreatedTime());
                            items.add(new ListViewItem(userInput, createdTime, serverResponse));
                            //System.out.println("#DEBUG: ON CHANGED RUN 3");
                        }

                        if (numMoments == 0) {
                            dayCompletionButton.setActivated(false);
                        } else {
                            dayCompletionButton.setActivated(true);
                        }

                        //System.out.println("#DEBUG: array size " + momentUiState.getMomentPairsList().size());
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
        addButton = footerView.findViewById(R.id.add_button);
        submitButton = footerView.findViewById(R.id.submit_button);
        submitButtonInactivate = footerView.findViewById(R.id.submit_button_inactivate);
        addButtonInactivate = footerView.findViewById(R.id.add_button_inactivated);
        //test
        dayCompletionButton = binding.dayCompleteButton;

        inputEditText = footerView.findViewById(R.id.inputEditText);
        addButtonText = footerView.findViewById(R.id.add_button_text);
        addButtonInactivateText = footerView.findViewById(R.id.add_button_inactivated_text);
        textCount = footerView.findViewById(R.id.textCount);
        constraintLayout = footerView.findViewById(R.id.edit_text_wrapper);
        // 초기 버튼 텍스트 설정
        textCount.setText("0/" + Integer.toString(MAX_LENGTH));
        scrollView = footerView.findViewById(R.id.listview_scroll);
        // 애니메이션
        Animation fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);

        // EditText의 텍스트 변경을 감지
        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 글자 수를 계산하고 버튼의 텍스트를 업데이트
                textCount.setText(s.length() + "/" + Integer.toString(MAX_LENGTH));
                // 글자 수에 따라 submitButton의 활성화/비활성화 상태 변경
                if (s.length() == 0) {
                    submitButton.setVisibility(View.GONE);
                    submitButtonInactivate.setVisibility(View.VISIBLE);
                } else {
                    submitButton.setVisibility(View.VISIBLE);
                    submitButtonInactivate.setVisibility(View.GONE);
                }

                // 글자 수가 1000자를 초과하면
                if (s.length() > MAX_LENGTH) {
                    // 1000자까지의 텍스트만 유지
                    inputEditText.setText(s.subSequence(0, MAX_LENGTH));
                    textCount.setTextColor(getResources().getColor(R.color.red)); // 수정된 부분
                    inputEditText.requestFocus();
                    // 커서를 텍스트 끝으로 이동
                    inputEditText.setSelection(MAX_LENGTH);
                }
            }
        });

        // 이건 submit button 누르면 키보드 사라지게
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
            INPUT_METHOD_SERVICE);

        addButton.setOnClickListener(v -> {

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm",
                Locale.getDefault());
            if (numMoments >= 2) {

                System.out.println("#Debug addbuton test1");
                String createdSecond = items.get(numMoments - 2).getInputTime();
                try {
                    System.out.println("#Debug addbuton test2");
                    Date createdDate = inputFormat.parse(createdSecond);
                    System.out.println("#Debug addbuton test3");
                    Calendar createdCalendar = Calendar.getInstance();
                    createdCalendar.setTime(createdDate);
                    int createdHourValue = createdCalendar.get(
                        Calendar.HOUR_OF_DAY); // This will give you the hour of createdSecond

                    System.out.println("#Debug addbuton test4");
                    Calendar currentCalendar = Calendar.getInstance();
                    int currentHourValue = currentCalendar.get(
                        Calendar.HOUR_OF_DAY); // This will give you the current hour

                    System.out.println("#Debug addbuton test5");
                    System.out.println(
                        "#Debug :: test :: current hour : " + currentHourValue + ", created hour: "
                            + createdHourValue);

                    if (createdHourValue == currentHourValue) {
                        addButton.setVisibility(View.GONE);
                        addButtonText.setVisibility(View.GONE);
                        addButtonInactivate.setVisibility(View.VISIBLE);
                        addButtonInactivateText.setVisibility(View.VISIBLE);
                    } else {
                        inputEditText.setVisibility(View.VISIBLE);
                        textCount.setVisibility(View.VISIBLE);
                        inputEditText.startAnimation(fadeIn);
                        textCount.startAnimation(fadeIn);

                        addButton.startAnimation(fadeOut);
                        addButtonText.startAnimation(fadeOut);
                        addButton.setVisibility(View.GONE);
                        addButtonText.setVisibility(View.GONE);

                        submitButtonInactivate.setVisibility(View.VISIBLE);
                        constraintLayout.setVisibility(View.VISIBLE);
                        submitButtonInactivate.startAnimation(fadeIn);
                        submitButtonInactivate.startAnimation(fadeIn);
                        binding.listviewList.setSelection(items.size() - 1);
                        // ScrollView를 ConstraintLayout의 하단으로 스크롤
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            } else {
                inputEditText.setVisibility(View.VISIBLE);
                textCount.setVisibility(View.VISIBLE);
                inputEditText.startAnimation(fadeIn);
                textCount.startAnimation(fadeIn);

                addButton.startAnimation(fadeOut);
                addButtonText.startAnimation(fadeOut);
                addButton.setVisibility(View.GONE);
                addButtonText.setVisibility(View.GONE);

                submitButtonInactivate.setVisibility(View.VISIBLE);
                constraintLayout.setVisibility(View.VISIBLE);
                submitButtonInactivate.startAnimation(fadeIn);
                constraintLayout.startAnimation(fadeIn);
                binding.listviewList.setSelection(items.size() - 1);
            }
        });
        addButtonInactivate.setOnClickListener(v -> {
            // null Listener
        });
        submitButton.setOnClickListener(v -> {
            String text = inputEditText.getText().toString();
            // 소프트 키보드 숨기기
            if (imm != null && getActivity().getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            }

            if (!text.isEmpty()) {
                viewModel.writeMoment(text);
                addItem(text);
                inputEditText.setText("");
                inputEditText.startAnimation(fadeOut);
                submitButton.startAnimation(fadeOut);
                submitButtonInactivate.startAnimation(fadeOut);
                textCount.startAnimation(fadeOut);
                constraintLayout.startAnimation(fadeOut);

                inputEditText.setVisibility(View.GONE);
                submitButton.setVisibility(View.GONE);
                submitButtonInactivate.setVisibility(View.GONE);
                textCount.setVisibility(View.GONE);
                constraintLayout.setVisibility(View.GONE);

                addButton.setVisibility(View.VISIBLE);
                addButtonText.setVisibility(View.VISIBLE);
                addButton.startAnimation(fadeIn);
                addButtonText.startAnimation(fadeIn);

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
