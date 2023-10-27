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
import snu.swpp.moment.utils.AnimationProvider;

public class MomentWriterContainer {

    private final ConstraintLayout momentEditTextWrapper;
    private final EditText momentEditText;
    private final TextView momentLengthText;
    private final TextView addButtonText;
    private final TextView addLimitWarnText;
    private final Button submitButton;
    private final Button submitButtonInactive;
    private final Button addButton;
    private final Button addButtonInactive;

    private final AnimationProvider animationProvider;

    private final int MOMENT_MAX_LENGTH = 100;

    public MomentWriterContainer(View view) {
        momentEditTextWrapper = view.findViewById(R.id.momentEditTextWrapper);
        momentEditText = view.findViewById(R.id.momentEditText);
        momentLengthText = view.findViewById(R.id.momentLengthText);
        addButtonText = view.findViewById(R.id.addHelpText);
        addLimitWarnText = view.findViewById(R.id.addLimitWarnText);

        submitButton = view.findViewById(R.id.submitButton);
        submitButtonInactive = view.findViewById(R.id.submitButtonInactive);
        addButton = view.findViewById(R.id.addButton);
        addButtonInactive = view.findViewById(R.id.addButtonInactive);

        animationProvider = new AnimationProvider(view);

        setMomentLengthText(0);

        // momentEditText 입력 감지
        momentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 글자 수를 계산
                setMomentLengthText(s.length());

                // 글자 수에 따라 submitButton의 활성화/비활성화 상태 변경
                if (s.length() == 0) {
                    submitButton.setVisibility(View.GONE);
                    submitButtonInactive.setVisibility(View.VISIBLE);
                } else if (s.length() > MOMENT_MAX_LENGTH) {
                    // 글자 수가 1000자를 초과하면 1000자까지의 텍스트만 유지
                    momentEditText.setText(s.subSequence(0, MOMENT_MAX_LENGTH));
                    momentLengthText.setTextColor(
                        ContextCompat.getColor(view.getContext(), R.color.red));
                    momentEditText.requestFocus();
                    // 커서를 텍스트 끝으로 이동
                    momentEditText.setSelection(MOMENT_MAX_LENGTH);
                } else {
                    momentLengthText.setTextColor(
                        ContextCompat.getColor(view.getContext(), R.color.black));
                    submitButton.setVisibility(View.VISIBLE);
                    submitButtonInactive.setVisibility(View.GONE);
                }
            }
        });

        addButtonInactive.setOnClickListener(v -> {
        });
    }

    public String getInputText() {
        return momentEditText.getText().toString();
    }

    public void setAddButtonOnClickListener(View.OnClickListener listener) {
        addButton.setOnClickListener(listener);
    }

    public void setSubmitButtonOnClickListener(View.OnClickListener listener) {
        submitButton.setOnClickListener(listener);
    }

    public void setUiWritingMoment() {
        // add 누르고 입력창 뜨는 동작
        addButton.startAnimation(animationProvider.fadeOut);
        addButtonText.startAnimation(animationProvider.fadeOut);
        addButton.setVisibility(View.GONE);
        addButtonText.setVisibility(View.GONE);

        momentEditTextWrapper.setVisibility(View.VISIBLE);
        momentEditTextWrapper.startAnimation(animationProvider.delayedFadeIn);
    }

    public void setUiReadyToAddMoment() {
        // submit 버튼 눌렀을 때 입력창 사라지고 add 버튼 표시되는 동작
        momentEditText.setText("");
        submitButton.setVisibility(View.GONE);
        submitButtonInactive.setVisibility(View.VISIBLE);
        momentEditTextWrapper.setVisibility(View.GONE);

        addButton.startAnimation(animationProvider.fadeIn);
        addButtonText.startAnimation(animationProvider.fadeIn);
        addButton.setVisibility(View.VISIBLE);
        addButtonText.setVisibility(View.VISIBLE);
    }

    public void setUiAddLimitExceeded() {
        // 모먼트 한 시간 2개 제한 초과했을 때
        addButton.setVisibility(View.GONE);
        addButtonText.setVisibility(View.GONE);

        addButtonInactive.setVisibility(View.VISIBLE);
        addLimitWarnText.setVisibility(View.VISIBLE);
    }

    public void setInvisible() {
        // 하루 마무리 시작되었을 때, 또는 과거 데이터 볼 때 모먼트 작성 칸 숨기기
        addButton.setVisibility(View.GONE);
        addButtonText.setVisibility(View.GONE);
        addLimitWarnText.setVisibility(View.GONE);

        submitButtonInactive.setVisibility(View.VISIBLE);
        momentEditTextWrapper.setVisibility(View.GONE);
    }

    private void setMomentLengthText(int count) {
        momentLengthText.setText(
            String.format(Locale.getDefault(), "%d / %d", count, MOMENT_MAX_LENGTH));
    }
}
