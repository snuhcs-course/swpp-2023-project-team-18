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
    private final EditText momentEditText;
    private final TextView momentLengthText;
    private final TextView addButtonText;
    private final TextView addLimitWarnText;
    private final Button submitButton;
    private final Button submitButtonInactivate;
    private final Button addButton;
    private final Button addButtonInactivate;

    private final ConstraintLayout storyWrapper;
    private final EditText storyTitleEditText;
    private final EditText storyContentEditText;
    private final TextView completeTimeText;
    private final TextView storyContentLengthText;
    private final TextView aiButtonHelpText;
    private final Button storyAiButton;

    private final BottomButtonContainer bottomButtonContainer;


    private final int MOMENT_MAX_LENGTH = 1000;
    private final int STORY_TITLE_MAX_LENGTH = 100;
    private final int STORY_CONTENT_MAX_LENGTH = 1000;

    public ListFooterContainer(View view) {
        // 모먼트 쓰기
        editTextWrapper = view.findViewById(R.id.edit_text_wrapper);
        momentEditText = view.findViewById(R.id.input_edit_text);
        momentLengthText = view.findViewById(R.id.moment_length_text);
        addButtonText = view.findViewById(R.id.add_button_text);
        addLimitWarnText = view.findViewById(R.id.add_limit_warn_text);
        submitButton = view.findViewById(R.id.submit_button);
        submitButtonInactivate = view.findViewById(R.id.submit_button_inactivate);
        addButton = view.findViewById(R.id.add_button);
        addButtonInactivate = view.findViewById(R.id.add_button_inactivate);

        // 하루 마무리
        storyWrapper = view.findViewById(R.id.story_wrapper);
        storyTitleEditText = view.findViewById(R.id.storyTitleEditText);
        storyContentEditText = view.findViewById(R.id.storyContentEditText);
        completeTimeText = view.findViewById(R.id.completeTimeText);
        storyContentLengthText = view.findViewById(R.id.storyContentLengthText);
        aiButtonHelpText = view.findViewById(R.id.aiButtonHelpText);
        storyAiButton = view.findViewById(R.id.storyAiButton);

        // 하단 버튼
        bottomButtonContainer = new BottomButtonContainer(view);

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
                // 글자 수를 계산하고 버튼의 텍스트를 업데이트
                setMomentLengthText(s.length());

                // 글자 수에 따라 submitButton의 활성화/비활성화 상태 변경
                if (s.length() == 0) {
                    submitButton.setVisibility(View.GONE);
                    submitButtonInactivate.setVisibility(View.VISIBLE);
                } else if (s.length() > MOMENT_MAX_LENGTH) {
                    // 글자 수가 1000자를 초과하면 1000자까지의 텍스트만 유지
                    momentEditText.setText(s.subSequence(0, MOMENT_MAX_LENGTH));
                    momentLengthText.setTextColor(
                        ContextCompat.getColor(view.getContext(), R.color.red));
                    momentEditText.requestFocus();
                    // 커서를 텍스트 끝으로 이동
                    momentEditText.setSelection(MOMENT_MAX_LENGTH);
                } else {
                    submitButton.setVisibility(View.VISIBLE);
                    submitButtonInactivate.setVisibility(View.GONE);
                }
            }
        });

        addButtonInactivate.setOnClickListener(v -> {
        });

        // storyContentEditText 입력 감지
        storyContentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 글자 수를 계산하고 버튼의 텍스트를 업데이트
                setStoryContentLengthText(s.length());

                // 글자 수에 따라 submitButton의 활성화/비활성화 상태 변경
                if (s.length() == 0) {
                    submitButton.setVisibility(View.GONE);
                    submitButtonInactivate.setVisibility(View.VISIBLE);
                } else if (s.length() > STORY_CONTENT_MAX_LENGTH) {
                    // 글자 수가 1000자를 초과하면 1000자까지의 텍스트만 유지
                    storyContentEditText.setText(s.subSequence(0, STORY_CONTENT_MAX_LENGTH));
                    storyContentEditText.setTextColor(
                        ContextCompat.getColor(view.getContext(), R.color.red));
                    storyContentEditText.requestFocus();
                    // 커서를 텍스트 끝으로 이동
                    storyContentEditText.setSelection(STORY_CONTENT_MAX_LENGTH);

                    // TODO: 버튼 활성화 상태 관리
                } else {

                }
            }
        });

        bottomButtonContainer.setStateObserver(buttonState -> {
            switch (buttonState) {
                case WRITING_MOMENT:
                    setUiWritingMoment();
                    break;
                case WRITING_STORY:
                    setUiWritingStory();
                    break;
            }
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
        editTextWrapper.setVisibility(View.VISIBLE);
        momentEditText.setVisibility(View.VISIBLE);
        momentLengthText.setVisibility(View.VISIBLE);
        submitButtonInactivate.setVisibility(View.VISIBLE);

        addButton.setVisibility(View.GONE);
        addButtonText.setVisibility(View.GONE);
    }

    public void setUiReadyToAdd() {
        addButton.setVisibility(View.VISIBLE);
        addButtonText.setVisibility(View.VISIBLE);

        momentEditText.setText("");
        momentEditText.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
        submitButtonInactivate.setVisibility(View.GONE);
        momentLengthText.setVisibility(View.GONE);
        editTextWrapper.setVisibility(View.GONE);
    }

    public void setUiLimitExceeded() {
        addButton.setVisibility(View.GONE);
        addButtonText.setVisibility(View.GONE);

        addButtonInactivate.setVisibility(View.VISIBLE);
        addLimitWarnText.setVisibility(View.VISIBLE);
    }

    public void setUiWritingStory() {
        addButton.setVisibility(View.GONE);
        addButtonText.setVisibility(View.GONE);
        addButtonInactivate.setVisibility(View.GONE);
        addLimitWarnText.setVisibility(View.GONE);
        editTextWrapper.setVisibility(View.GONE);
        momentEditText.setVisibility(View.GONE);
        momentLengthText.setVisibility(View.GONE);
        submitButtonInactivate.setVisibility(View.GONE);

        storyWrapper.setVisibility(View.VISIBLE);
    }

    private void setMomentLengthText(int count) {
        momentLengthText.setText(
            String.format(Locale.getDefault(), "%d / %d", count, MOMENT_MAX_LENGTH));
    }

    private void setStoryContentLengthText(int count) {
        storyContentLengthText.setText(
            String.format(Locale.getDefault(), "%d / %d", count, STORY_CONTENT_MAX_LENGTH));
    }
}
