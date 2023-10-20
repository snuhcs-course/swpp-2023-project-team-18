package snu.swpp.moment.ui.main_writeview.DaySlide;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import snu.swpp.moment.MainActivity;
import snu.swpp.moment.R;
import snu.swpp.moment.databinding.TodayItemBinding;
import snu.swpp.moment.ui.main_writeview.ListView_Adapter;
import snu.swpp.moment.ui.main_writeview.ListView_Item;
import snu.swpp.moment.utils.KeyboardUtils;

public class TodayViewFragment extends Fragment {
    private TodayItemBinding binding;
    private List<ListView_Item> items;
    private ListView_Adapter mAdapter;
    private Button addButton, submitButton, submitButtonInactivate;
    private ListView listView;
    private EditText inputEditText;
    private TextView textCount, addButtonText;

    private ConstraintLayout constraintLayout;

    private int MAX_LENGTH = 100;
    private ScrollView scrollView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = TodayItemBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // Initialize ListView and related components
        initializeListView(root);
        KeyboardUtils.hideKeyboardOnOutsideTouch(root, getActivity());

        // 루트 뷰에 터치 리스너 설정

        return root;
    }

    private void initializeListView(View root) {
        listView = root.findViewById(R.id.listview_list);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        String formattedDate = sdf.format(calendar.getTime());
        items = new ArrayList<>();
        mAdapter = new ListView_Adapter(getContext(), items);
        listView.setAdapter(mAdapter);
        View footerView = LayoutInflater.from(getContext()).inflate(R.layout.listview_footer, listView, false);
        listView.addFooterView(footerView);
        addButton = footerView.findViewById(R.id.add_button);
        submitButton = footerView.findViewById(R.id.submit_button);
        submitButtonInactivate = footerView.findViewById(R.id.submit_button_inactivate);
        inputEditText = footerView.findViewById(R.id.inputEditText);
        addButtonText = footerView.findViewById(R.id.add_button_text);
        textCount = footerView.findViewById(R.id.textCount);
        constraintLayout = footerView.findViewById(R.id.edit_text_wrapper);
        // 초기 버튼 텍스트 설정
        textCount.setText("0/"+Integer.toString(MAX_LENGTH));
        scrollView = footerView.findViewById(R.id.listview_scroll);



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
                textCount.setText(s.length() + "/"+Integer.toString(MAX_LENGTH));
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
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        addButton.setOnClickListener(v -> {
            inputEditText.setVisibility(View.VISIBLE);
            textCount.setVisibility(View.VISIBLE);

            addButton.setVisibility(View.GONE);
            addButtonText.setVisibility(View.GONE);
            submitButtonInactivate.setVisibility(View.VISIBLE);
            constraintLayout.setVisibility(View.VISIBLE);
            // 아래 줄 있으면, 텍스트 입력이 박스 넘어가도 줄바꿈이 안됨
            //inputEditText.setSingleLine(true);
            //submitButton.setVisibility(View.VISIBLE);

            // ScrollView를 ConstraintLayout의 하단으로 스크롤


        });
        submitButton.setOnClickListener(v -> {
            String text = inputEditText.getText().toString();
            // 소프트 키보드 숨기기
            if (imm != null && getActivity().getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            }


            if (!text.isEmpty()) {
                addItem(text);
                inputEditText.setText("");
                inputEditText.setVisibility(View.GONE);
                submitButton.setVisibility(View.GONE);
                submitButtonInactivate.setVisibility(View.GONE);
                textCount.setVisibility(View.GONE);

                addButton.setVisibility(View.VISIBLE);
                addButtonText.setVisibility(View.VISIBLE);


                constraintLayout.setVisibility(View.GONE);

            }
        });
    }

    private void addItem(String userInput) {
        String currentTime = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(new Date());
        items.add(new ListView_Item(userInput, currentTime, ""));
        mAdapter.notifyDataSetChanged();
        listView.setSelection(items.size() - 1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
