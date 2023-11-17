package snu.swpp.moment.ui.main_writeview.component;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import java.util.Date;
import java.util.Locale;
import snu.swpp.moment.R;
import snu.swpp.moment.utils.AnimationProvider;
import snu.swpp.moment.utils.TimeConverter;


enum StoryContainerState {
    INVISIBLE,
    WRITING,
    COMPLETE,
}


public class StoryContainer {

    private StoryContainerState state;

    private final ConstraintLayout storyWrapper;
    private final EditText storyTitleEditText;
    private final EditText storyContentEditText;
    private final TextView completeTimeText;
    private final TextView storyContentLengthText;
    private final TextView aiButtonHelpText;
    private final Button storyAiButton;

    private final AnimationProvider animationProvider;

    // 글자수 제한 초과 검사
    private boolean isTitleLimitExceeded = false;
    private boolean isContentLimitExceeded = false;
    private final MutableLiveData<Boolean> isLimitExceeded = new MutableLiveData<>(false);

    // AI에게 부탁하기 버튼
    private final MutableLiveData<Boolean> aiButtonSwitch = new MutableLiveData<>(false);

    private final int STORY_TITLE_MAX_LENGTH = 20;
    private final int STORY_CONTENT_MAX_LENGTH = 1000;

    public StoryContainer(@NonNull View view) {
        storyWrapper = (ConstraintLayout) view;
        storyTitleEditText = view.findViewById(R.id.storyTitleEditText);
        storyContentEditText = view.findViewById(R.id.storyContentEditText);
        completeTimeText = view.findViewById(R.id.completeTimeText);
        storyContentLengthText = view.findViewById(R.id.storyContentLengthText);
        aiButtonHelpText = view.findViewById(R.id.aiButtonHelpText);
        storyAiButton = view.findViewById(R.id.storyAiButton);

        animationProvider = new AnimationProvider(view);

        // storyTitleEditText 입력 감지
        storyTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > STORY_TITLE_MAX_LENGTH) {
                    // 글자수 제한 초과
                    storyTitleEditText.setText(s.subSequence(0, STORY_TITLE_MAX_LENGTH));
                    storyTitleEditText.setTextColor(
                        ContextCompat.getColor(view.getContext(), R.color.red));
                    storyTitleEditText.requestFocus();
                    storyTitleEditText.setSelection(STORY_TITLE_MAX_LENGTH);

                    isTitleLimitExceeded = true;
                    checkLimitExceeded();
                } else if (isTitleLimitExceeded) {
                    storyTitleEditText.setTextColor(
                        ContextCompat.getColor(view.getContext(), R.color.black));

                    isTitleLimitExceeded = false;
                    checkLimitExceeded();
                }
            }
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
                setStoryContentLengthText(s.length());

                if (s.length() > STORY_CONTENT_MAX_LENGTH) {
                    // 글자수 제한 초과
                    storyContentEditText.setText(s.subSequence(0, STORY_CONTENT_MAX_LENGTH));
                    storyContentEditText.setTextColor(
                        ContextCompat.getColor(view.getContext(), R.color.red));
                    storyContentEditText.requestFocus();
                    storyContentEditText.setSelection(STORY_CONTENT_MAX_LENGTH);

                    isContentLimitExceeded = true;
                    checkLimitExceeded();
                } else if (isContentLimitExceeded) {
                    storyContentEditText.setTextColor(
                        ContextCompat.getColor(view.getContext(), R.color.black));

                    isContentLimitExceeded = false;
                    checkLimitExceeded();
                }
            }
        });

        // AI에게 부탁하기 버튼
        storyAiButton.setOnClickListener(v -> {
            if (getStoryContent().isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(),
                    R.style.DialogTheme);
                builder.setMessage(R.string.ai_story_popup);

                builder.setPositiveButton(R.string.popup_yes, (dialog, id) -> {
                    // AI 버튼 숨기고 API 호출
                    setAiButtonVisibility(View.GONE);
                    setAiButtonSwitch();
                });
                builder.setNegativeButton(R.string.popup_no, (dialog, id) -> {
                });
                builder.create().show();
            } else {
                // 내용을 이미 작성했으면 AI 요약 불가
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(),
                    R.style.DialogTheme);
                builder.setMessage(R.string.ai_story_unavailable_popup);
                builder.setPositiveButton(R.string.popup_ok, (dialog, id) -> {
                });
                builder.create().show();
            }
        });
    }

    public void setState(StoryContainerState state) {
        Log.d("StoryContainer", "setState: " + state);
        this.state = state;

        updateStoryWrapper();
        updateStoryEditText();
        updateAiButton();
    }

    private void updateStoryWrapper() {
        switch (state) {
            case INVISIBLE:
                storyWrapper.setVisibility(View.GONE);
                break;
            case WRITING:
                storyWrapper.setVisibility(View.VISIBLE);
                storyWrapper.startAnimation(animationProvider.fadeIn);
                break;
            case COMPLETE:
                storyWrapper.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updateStoryEditText() {
        switch (state) {
            case INVISIBLE:
                setStoryText("", "");
                storyTitleEditText.setEnabled(false);
                storyContentEditText.setEnabled(false);
                storyContentLengthText.setVisibility(View.VISIBLE);
                setStoryContentLengthText(0);
                break;
            case WRITING:
                storyTitleEditText.setEnabled(true);
                storyContentEditText.setEnabled(true);
                storyContentLengthText.setVisibility(View.VISIBLE);
                break;
            case COMPLETE:
                storyTitleEditText.setEnabled(false);
                storyContentEditText.setEnabled(false);
                storyContentLengthText.setVisibility(View.GONE);
                setStoryContentLengthText(0);
                break;
        }
    }

    private void updateAiButton() {
        switch (state) {
            case INVISIBLE:
            case WRITING:
                setAiButtonVisibility(View.VISIBLE);
                break;
            case COMPLETE:
                setAiButtonVisibility(View.GONE);
                break;
        }
    }

    public void setCompletedDate(Date date) {
        completeTimeText.setText(TimeConverter.formatDate(date, "HH:mm"));
    }

    public String getStoryTitle() {
        return storyTitleEditText.getText().toString();
    }

    public String getStoryContent() {
        return storyContentEditText.getText().toString();
    }

    public void observeLimit(Observer<Boolean> observer) {
        isLimitExceeded.observeForever(observer);
    }

    public void observeAiButtonSwitch(Observer<Boolean> observer) {
        aiButtonSwitch.observeForever(observer);
    }

    public void removeObservers(LifecycleOwner lifecycleOwner) {
        isLimitExceeded.removeObservers(lifecycleOwner);
        aiButtonSwitch.removeObservers(lifecycleOwner);
    }

//    public void resetUi() {
//        storyWrapper.setVisibility(View.GONE);
//
//        setStoryText("", "");
//        freeze(false);
//
//        storyContentLengthText.setVisibility(View.VISIBLE);
//        setStoryContentLengthText(0);
//
//        storyAiButton.setVisibility(View.VISIBLE);
//        aiButtonHelpText.setVisibility(View.VISIBLE);
//    }

//    public void setUiWritingStory(Date completeTime) {
//        // 과거 스토리: 서버에서 받아온 createdAt 표시
//        storyWrapper.setVisibility(View.VISIBLE);
//        setCompleteTimeText(completeTime);
//        storyWrapper.startAnimation(animationProvider.fadeIn);
//    }

//    public void setUiWritingStory() {
//        // 오늘 스토리: 현재 시간 표시
//        Date completeTime = new Date();
//        setUiWritingStory(completeTime);
//    }

//    public void setUiCompleteStory() {
//        storyContentLengthText.setVisibility(View.GONE);
//        aiButtonHelpText.setVisibility(View.GONE);
//        storyAiButton.setVisibility(View.GONE);
//    }

    // FIXME: will be deleted
    public void freeze(boolean freeze) {
        storyTitleEditText.setEnabled(!freeze);
        storyContentEditText.setEnabled(!freeze);
        if (freeze) {
            storyTitleEditText.setHint("");
            storyContentEditText.setHint("");
        } else {
            storyTitleEditText.setHint(R.string.story_title_hint);
            storyContentEditText.setHint(R.string.story_content_hint);
        }
    }

    public void setStoryText(String title, String content) {
        storyTitleEditText.setText(title);
        storyContentEditText.setText(content);
    }

    public void setAiButtonVisibility(int visibility) {
        storyAiButton.setVisibility(visibility);
        aiButtonHelpText.setVisibility(visibility);
    }

//    private void setCompleteTimeText(Date completeTime) {
//        completeTimeText.setText(TimeConverter.formatDate(completeTime, "HH:mm"));
//    }

    private void checkLimitExceeded() {
        isLimitExceeded.setValue(isTitleLimitExceeded || isContentLimitExceeded);
    }

    private void setStoryContentLengthText(int count) {
        storyContentLengthText.setText(
            String.format(Locale.getDefault(), "%d / %d", count, STORY_CONTENT_MAX_LENGTH));
    }

    private void setAiButtonSwitch() {
        aiButtonSwitch.setValue(true);
        aiButtonSwitch.setValue(false);
    }
}
