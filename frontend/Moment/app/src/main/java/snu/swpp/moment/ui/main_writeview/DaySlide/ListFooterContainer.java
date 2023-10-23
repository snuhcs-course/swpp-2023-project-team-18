package snu.swpp.moment.ui.main_writeview.DaySlide;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import java.util.Locale;
import snu.swpp.moment.R;

public class ListFooterContainer {

    private final ConstraintLayout editTextWrapper;
    private final EditText inputEditText;
    private final TextView textCount;
    private final TextView addButtonText;
    private final TextView addLimitWarnText;
    private final Button submitButton;
    private final Button submitButtonInactivate;
    private final Button addButton;
    private final Button addButtonInactivate;

    private final int MOMENT_MAX_LENGTH = 1000;

    public ListFooterContainer(View view) {
        editTextWrapper = view.findViewById(R.id.edit_text_wrapper);
        inputEditText = view.findViewById(R.id.input_edit_text);
        textCount = view.findViewById(R.id.text_count);
        addButtonText = view.findViewById(R.id.add_button_text);
        addLimitWarnText = view.findViewById(R.id.add_limit_warn_text);
        submitButton = view.findViewById(R.id.submit_button);
        submitButtonInactivate = view.findViewById(R.id.submit_button_inactivate);
        addButton = view.findViewById(R.id.add_button);
        addButtonInactivate = view.findViewById(R.id.add_button_inactivate);

        setTextCount(0);

        // inputEditText 입력 감지
        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 글자 수를 계산하고 버튼의 텍스트를 업데이트
                setTextCount(s.length());
                // 글자 수에 따라 submitButton의 활성화/비활성화 상태 변경
                if (s.length() == 0) {
                    submitButton.setVisibility(View.GONE);
                    submitButtonInactivate.setVisibility(View.VISIBLE);
                } else {
                    submitButton.setVisibility(View.VISIBLE);
                    submitButtonInactivate.setVisibility(View.GONE);
                }

                // 글자 수가 1000자를 초과하면
                if (s.length() > MOMENT_MAX_LENGTH) {
                    // 1000자까지의 텍스트만 유지
                    inputEditText.setText(s.subSequence(0, MOMENT_MAX_LENGTH));
                    textCount.setTextColor(ContextCompat.getColor(view.getContext(), R.color.red));
                    inputEditText.requestFocus();
                    // 커서를 텍스트 끝으로 이동
                    inputEditText.setSelection(MOMENT_MAX_LENGTH);
                }
            }
        });

        addButtonInactivate.setOnClickListener(v -> {
        });
    }

    public String getInputText() {
        return inputEditText.getText().toString();
    }

    public void setAddButtonOnClickListener(View.OnClickListener listener) {
        addButton.setOnClickListener(listener);
    }

    public void setSubmitButtonOnClickListener(View.OnClickListener listener) {
        submitButton.setOnClickListener(listener);
    }

    public void setUiWriting() {
        editTextWrapper.setVisibility(View.VISIBLE);
        inputEditText.setVisibility(View.VISIBLE);
        textCount.setVisibility(View.VISIBLE);
        submitButtonInactivate.setVisibility(View.VISIBLE);

        addButton.setVisibility(View.GONE);
        addButtonText.setVisibility(View.GONE);
    }

    public void setUiReadyToAdd() {
        addButton.setVisibility(View.VISIBLE);
        addButtonText.setVisibility(View.VISIBLE);

        inputEditText.setText("");
        inputEditText.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
        submitButtonInactivate.setVisibility(View.GONE);
        textCount.setVisibility(View.GONE);
        editTextWrapper.setVisibility(View.GONE);
    }

    public void setUiMomentLimitExceeded() {
        addButton.setVisibility(View.GONE);
        addButtonText.setVisibility(View.GONE);

        addButtonInactivate.setVisibility(View.VISIBLE);
        addLimitWarnText.setVisibility(View.VISIBLE);
    }

    private void setTextCount(int count) {
        textCount.setText(String.format(Locale.getDefault(), "%d/%d", count, MOMENT_MAX_LENGTH));
    }
}
