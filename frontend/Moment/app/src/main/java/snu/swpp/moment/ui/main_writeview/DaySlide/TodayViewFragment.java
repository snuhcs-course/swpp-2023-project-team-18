package snu.swpp.moment.ui.main_writeview.DaySlide;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
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

public class TodayViewFragment extends Fragment {
    private TodayItemBinding binding;
    private List<ListView_Item> items;
    private ListView_Adapter mAdapter;
    private Button addButton, submitButton, submitButtonInactivate;
    private ListView listView;
    private EditText inputEditText;
    private TextView textCount, addButtonText;

    private int MAX_LENGTH = 100;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = TodayItemBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // Initialize ListView and related components
        initializeListView(root);
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

        // 초기 버튼 텍스트 설정
        textCount.setText("0/"+Integer.toString(MAX_LENGTH));

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
                    // 커서를 텍스트 끝으로 이동
                    // 커서를 텍스트 끝으로 이동
                    inputEditText.setSelection(MAX_LENGTH);
                }
            }
        });

        addButton.setOnClickListener(v -> {
            inputEditText.setVisibility(View.VISIBLE);

            textCount.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.GONE);
            addButtonText.setVisibility(View.GONE);
            submitButtonInactivate.setVisibility(View.VISIBLE);

            // 아래 줄 있으면, 텍스트 입력이 박스 넘어가도 줄바꿈이 안됨
            //inputEditText.setSingleLine(true);
            //submitButton.setVisibility(View.VISIBLE);
        });

        submitButton.setOnClickListener(v -> {
            String text = inputEditText.getText().toString();
            if (!text.isEmpty()) {
                addItem(text);
                inputEditText.setText("");
                inputEditText.setVisibility(View.GONE);
                submitButton.setVisibility(View.GONE);
                textCount.setVisibility(View.GONE);
                addButton.setVisibility(View.VISIBLE);
                submitButtonInactivate.setVisibility(View.GONE);
                addButtonText.setVisibility(View.VISIBLE);
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
