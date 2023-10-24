package snu.swpp.moment.ui.main_writeview.DaySlide;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import java.util.Locale;
import snu.swpp.moment.R;
import snu.swpp.moment.ui.main_writeview.EmotionGridContainer;

public class ListFooterContainer {

    // 모먼트 작성
    private final ConstraintLayout editTextWrapper;
    private final EditText momentEditText;
    private final TextView momentLengthText;
    private final TextView addButtonText;
    private final TextView addLimitWarnText;
    private final Button submitButton;
    private final Button submitButtonInactivate;
    private final Button addButton;
    private final Button addButtonInactivate;

    // 스토리 작성
    private final StoryContainer storyContainer;

    // 감정 선택
    private final ConstraintLayout emotionWrapper;
    private final TextView emotionHelpText;
    private final EmotionGridContainer emotionGridContainer;

    // 태그 입력
    private final ConstraintLayout tagWrapper;
    private final TextView tagHelpText;
    private final TagBoxContainer tagBoxContainer;

    // 하단 버튼 활성화 상태
    private final MutableLiveData<Boolean> bottomButtonState = new MutableLiveData<>(false);

    private final int MOMENT_MAX_LENGTH = 1000;

    public ListFooterContainer(@NonNull View view) {
        // 모먼트 작성
        editTextWrapper = view.findViewById(R.id.edit_text_wrapper);
        momentEditText = view.findViewById(R.id.input_edit_text);
        momentLengthText = view.findViewById(R.id.moment_length_text);
        addButtonText = view.findViewById(R.id.add_button_text);
        addLimitWarnText = view.findViewById(R.id.add_limit_warn_text);
        submitButton = view.findViewById(R.id.submit_button);
        submitButtonInactivate = view.findViewById(R.id.submit_button_inactivate);
        addButton = view.findViewById(R.id.add_button);
        addButtonInactivate = view.findViewById(R.id.add_button_inactivate);

        // 스토리 작성
        storyContainer = new StoryContainer(view.findViewById(R.id.story_wrapper));

        // 감정 선택
        emotionWrapper = view.findViewById(R.id.emotion_wrapper);
        emotionHelpText = view.findViewById(R.id.emotion_help_text);
        emotionGridContainer = new EmotionGridContainer(view.findViewById(R.id.emotion_selector));

        // 태그 입력
        tagWrapper = view.findViewById(R.id.tag_wrapper);
        tagHelpText = view.findViewById(R.id.tag_help_text);
        tagBoxContainer = new TagBoxContainer(view.findViewById(R.id.tag_box));

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
                    momentLengthText.setTextColor(
                        ContextCompat.getColor(view.getContext(), R.color.black));
                    submitButton.setVisibility(View.VISIBLE);
                    submitButtonInactivate.setVisibility(View.GONE);
                }
            }
        });

        addButtonInactivate.setOnClickListener(v -> {
        });

        // 감정 선택 감지
        emotionGridContainer.setSelectedEmotionObserver((Integer emotion) -> {
            setBottomButtonState(emotion > -1);
        });

        // 태그 개수 제한 감지
        tagBoxContainer.setLimitObserver((Boolean isLimitExceeded) -> {
            setBottomButtonState(!isLimitExceeded);
        });
    }

    public String getInputText() {
        return momentEditText.getText().toString();
    }

    public void setBottomButtonState(boolean state) {
        bottomButtonState.setValue(state);
    }

    public void setAddButtonOnClickListener(View.OnClickListener listener) {
        addButton.setOnClickListener(listener);
    }

    public void setSubmitButtonOnClickListener(View.OnClickListener listener) {
        submitButton.setOnClickListener(listener);
    }

    public void setBottomButtonStateObserver(Observer<Boolean> observer) {
        bottomButtonState.observeForever(observer);
    }

    public void freezeStoryEditText() {
        storyContainer.freeze();
    }

    public void freezeEmotionSelector() {
        emotionGridContainer.freeze();
    }

    public void setUiWritingMoment() {
        editTextWrapper.setVisibility(View.VISIBLE);
        momentEditText.setVisibility(View.VISIBLE);
        momentLengthText.setVisibility(View.VISIBLE);
        submitButtonInactivate.setVisibility(View.VISIBLE);

        addButton.setVisibility(View.GONE);
        addButtonText.setVisibility(View.GONE);

        setBottomButtonState(false);
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

        setBottomButtonState(true);
    }

    public void setUiAddLimitExceeded() {
        addButton.setVisibility(View.GONE);
        addButtonText.setVisibility(View.GONE);

        addButtonInactivate.setVisibility(View.VISIBLE);
        addLimitWarnText.setVisibility(View.VISIBLE);

        setBottomButtonState(true);
    }

    public void setUiWritingStory(String completeTime) {
        addButton.setVisibility(View.GONE);
        addButtonText.setVisibility(View.GONE);
        addButtonInactivate.setVisibility(View.GONE);
        addLimitWarnText.setVisibility(View.GONE);
        editTextWrapper.setVisibility(View.GONE);
        momentEditText.setVisibility(View.GONE);
        momentLengthText.setVisibility(View.GONE);
        submitButtonInactivate.setVisibility(View.GONE);

        storyContainer.setUiWritingStory(completeTime);

        setBottomButtonState(true);
    }

    public void setUiSelectingEmotion() {
        storyContainer.setUiCompleteStory();

        emotionWrapper.setVisibility(View.VISIBLE);

        setBottomButtonState(false);
    }

    public void setUiWritingTags() {
        tagWrapper.setVisibility(View.VISIBLE);
    }

    private void setMomentLengthText(int count) {
        momentLengthText.setText(
            String.format(Locale.getDefault(), "%d / %d", count, MOMENT_MAX_LENGTH));
    }
}
