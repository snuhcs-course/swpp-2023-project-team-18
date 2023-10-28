package snu.swpp.moment.ui.main_writeview.DaySlide;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import snu.swpp.moment.R;
import snu.swpp.moment.utils.AnimationProvider;
import snu.swpp.moment.utils.TimeConverter;

public class StoryContainer {

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
                } else {
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
                } else {
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
                    hideAiButton();

                    // TODO: API 호출해서 AI 요약 받아오기
                    Map<String, String> result = new HashMap<>() {{
                        put("title", "AI 요약 제목");
                        put("content", "AI 요약 내용");
                    }};
                    setStoryText(result.get("title"), result.get("content"));
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

    public String getStoryTitle() {
        return storyTitleEditText.getText().toString();
    }

    public String getStoryContent() {
        return storyContentEditText.getText().toString();
    }

    public void freeze() {
        storyTitleEditText.setEnabled(false);
        storyTitleEditText.setHint("");
        storyContentEditText.setEnabled(false);
        storyContentEditText.setHint("");
    }

    public void setLimitObserver(Observer<Boolean> observer) {
        isLimitExceeded.observeForever(observer);
    }

    public void setUiWritingStory(Date completeTime) {
        storyWrapper.setVisibility(View.VISIBLE);
        setCompleteTimeText(completeTime);
        storyWrapper.startAnimation(animationProvider.fadeIn);
    }

    public void setUiCompleteStory() {
        storyContentLengthText.setVisibility(View.GONE);
        aiButtonHelpText.setVisibility(View.GONE);
        storyAiButton.setVisibility(View.GONE);
    }

    public void setCompleteTimeText(Date completeTime) {
        completeTimeText.setText(TimeConverter.formatDate(completeTime, "HH:mm"));
    }

    public void setStoryText(String title, String content) {
        storyTitleEditText.setText(title);
        storyContentEditText.setText(content);
    }

    private void checkLimitExceeded() {
        isLimitExceeded.setValue(isTitleLimitExceeded || isContentLimitExceeded);
    }

    private void setStoryContentLengthText(int count) {
        storyContentLengthText.setText(
            String.format(Locale.getDefault(), "%d / %d", count, STORY_CONTENT_MAX_LENGTH));
    }

    private void hideAiButton() {
        storyAiButton.setVisibility(View.INVISIBLE);
        aiButtonHelpText.setVisibility(View.INVISIBLE);
    }
}