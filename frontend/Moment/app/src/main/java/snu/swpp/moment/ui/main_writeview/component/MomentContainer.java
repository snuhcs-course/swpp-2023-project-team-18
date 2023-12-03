package snu.swpp.moment.ui.main_writeview.component;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import java.util.Locale;
import snu.swpp.moment.R;
import snu.swpp.moment.utils.AnimationProvider;


enum MomentContainerState {
    INVISIBLE, READY_TO_ADD, WRITING, WAITING_AI_REPLY, ADD_LIMIT_EXCEEDED,
}


public class MomentContainer {

    private MomentContainerState state;

    private final ConstraintLayout momentEditTextWrapper;
    private final EditText momentEditText;
    private final TextView momentLengthText;
    private final TextView addButtonText;
    private final TextView addLimitWarnText;
    private final Button submitButton;
    private final Button submitButtonInactive;
    private final TextView submitButtonText;
    private final TextView submitButtonTextInactive;
    private final Button addButton;
    private final Button addButtonInactive;

    private final AnimationProvider animationProvider;

    private final int MOMENT_MAX_LENGTH = 100;

    public MomentContainer(View view) {
        momentEditTextWrapper = view.findViewById(R.id.momentEditTextWrapper);
        momentEditText = view.findViewById(R.id.momentEditText);
        momentLengthText = view.findViewById(R.id.momentLengthText);
        submitButton = view.findViewById(R.id.submitButton);
        submitButtonInactive = view.findViewById(R.id.submitButtonInactive);
        submitButtonText = view.findViewById(R.id.submitHelpText);
        submitButtonTextInactive = view.findViewById(R.id.submitHelpTextInactive);
        addButtonText = view.findViewById(R.id.addHelpText);
        addLimitWarnText = view.findViewById(R.id.addLimitWarnText);

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
                    setSubmitButtonActivated(false);
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
                    setSubmitButtonActivated(true);
                }
            }
        });

        // addButtonInactive.setOnClickListener(v -> {
        //  });
    }

    public void setState(MomentContainerState state) {
        Log.d("MomentWriterContainer", "setState: " + state);
        this.state = state;

        updateAddButton();
        updateMomentEditTextWrapper();
        updateSubmitButton();
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

    private void updateAddButton() {
        switch (state) {
            case INVISIBLE:
                addButton.setVisibility(View.GONE);
                addButtonInactive.setVisibility(View.GONE);
                addButtonText.setVisibility(View.GONE);
                submitButton.setVisibility(View.GONE);
                submitButtonInactive.setVisibility(View.GONE);
                submitButtonText.setVisibility(View.GONE);
                submitButtonTextInactive.setVisibility(View.GONE);
                addLimitWarnText.setVisibility(View.GONE);
                break;
            case READY_TO_ADD:
                addButton.startAnimation(animationProvider.fadeIn);
                addButton.setVisibility(View.VISIBLE);
                addButtonText.startAnimation(animationProvider.fadeIn);
                addButtonText.setVisibility(View.VISIBLE);
                addButtonInactive.setVisibility(View.GONE);
                submitButton.setVisibility(View.GONE);
                submitButtonText.setVisibility(View.GONE);
                submitButtonInactive.setVisibility(View.GONE);
                submitButtonText.setVisibility(View.GONE);
                submitButtonTextInactive.setVisibility(View.GONE);
                addLimitWarnText.setVisibility(View.GONE);
                break;
            case WRITING:
                addButton.startAnimation(animationProvider.fadeOut);
                addButton.setVisibility(View.GONE);
                addButtonText.startAnimation(animationProvider.fadeOut);
                addButtonText.setVisibility(View.GONE);
                addButtonInactive.setVisibility(View.GONE);
                submitButtonInactive.startAnimation(animationProvider.delayedFadeIn);
                submitButtonInactive.setVisibility(View.VISIBLE);
                submitButton.setVisibility(View.GONE);
                submitButtonTextInactive.startAnimation(animationProvider.delayedFadeIn);
                submitButtonTextInactive.setVisibility(View.VISIBLE);
                submitButtonText.setVisibility(View.GONE);
                addLimitWarnText.setVisibility(View.GONE);
            case WAITING_AI_REPLY:
                addButton.setVisibility(View.GONE);
                addButtonText.setVisibility(View.GONE);
                submitButton.setVisibility(View.GONE);
                submitButtonText.setVisibility(View.GONE);
                submitButtonInactive.setVisibility(View.GONE);
                submitButtonText.setVisibility(View.GONE);
                submitButtonTextInactive.setVisibility(View.GONE);
                addLimitWarnText.setVisibility(View.GONE);
                break;
            case ADD_LIMIT_EXCEEDED:
                addButton.setVisibility(View.GONE);
                addButtonInactive.setVisibility(View.VISIBLE);
                addButtonText.setVisibility(View.GONE);
                submitButton.setVisibility(View.GONE);
                submitButtonText.setVisibility(View.GONE);
                submitButtonInactive.setVisibility(View.GONE);
                submitButtonText.setVisibility(View.GONE);
                submitButtonTextInactive.setVisibility(View.GONE);
                addLimitWarnText.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updateMomentEditTextWrapper() {
        switch (state) {
            case INVISIBLE:
            case READY_TO_ADD:
            case ADD_LIMIT_EXCEEDED:
                momentEditTextWrapper.setVisibility(View.GONE);
                break;

            case WRITING:
                momentEditTextWrapper.setVisibility(View.VISIBLE);
                momentEditTextWrapper.startAnimation(animationProvider.delayedFadeIn);
                break;
            case WAITING_AI_REPLY:
                momentEditTextWrapper.setVisibility(View.GONE);
                momentEditText.setText("");
                break;
        }
    }

    private void updateSubmitButton() {
        setSubmitButtonActivated(false);
    }

    private void setSubmitButtonActivated(boolean activated) {
        if (state != MomentContainerState.WRITING) {
            return;
        }
        if (activated) {
            submitButton.setVisibility(View.VISIBLE);
            submitButtonInactive.setVisibility(View.GONE);
            submitButtonText.setVisibility(View.VISIBLE);
            submitButtonTextInactive.setVisibility(View.GONE);


        } else {
            submitButton.setVisibility(
                View.GONE);
            submitButtonInactive.setVisibility(View.VISIBLE);
            submitButtonText.setVisibility(View.GONE);
            submitButtonTextInactive.setVisibility(View.VISIBLE);
        }
    }

    private void setMomentLengthText(int count) {
        momentLengthText.setText(
            String.format(Locale.getDefault(), "%d / %d", count, MOMENT_MAX_LENGTH));
    }
}
