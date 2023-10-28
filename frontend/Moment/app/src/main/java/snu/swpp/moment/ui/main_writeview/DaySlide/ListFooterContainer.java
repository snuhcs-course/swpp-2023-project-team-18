package snu.swpp.moment.ui.main_writeview.DaySlide;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import snu.swpp.moment.R;
import snu.swpp.moment.ui.main_writeview.EmotionGridContainer;
import snu.swpp.moment.ui.main_writeview.uistate.AiStoryState;
import snu.swpp.moment.ui.main_writeview.uistate.StoryUiState;
import snu.swpp.moment.utils.AnimationProvider;

public class ListFooterContainer {

    private final View view;

    // 모먼트 작성
    private final MomentWriterContainer momentWriterContainer;
    // 스토리 작성
    private final StoryContainer storyContainer;
    // 감정 선택
    private final ConstraintLayout emotionWrapper;
    private final EmotionGridContainer emotionGridContainer;
    // 태그 입력
    private final TagBoxContainer tagBoxContainer;
    // 점수 선택
    private final ScoreContainer scoreContainer;
    // 로딩 메시지
    private final TextView loadingText;

    // 애니메이션 개체 묶음
    private final AnimationProvider animationProvider;

    // 하단 버튼 활성화 상태
    private final MutableLiveData<Boolean> bottomButtonState = new MutableLiveData<>(false);

    // 새 요소 추가 시 하단으로 스크롤 하기 위한 스위치
    private final MutableLiveData<Boolean> scrollToBottomSwitch = new MutableLiveData<>(false);
    // AI 요약 API call을 위한 스위치
    private final MutableLiveData<Boolean> aiStoryCallSwitch = new MutableLiveData<>(false);


    public ListFooterContainer(@NonNull View view) {
        this.view = view;

        // 모먼트 작성
        momentWriterContainer = new MomentWriterContainer(view.findViewById(R.id.moment_writer));
        // 스토리 작성
        storyContainer = new StoryContainer(view.findViewById(R.id.story_wrapper));
        // 감정 선택
        emotionWrapper = view.findViewById(R.id.emotion_wrapper);
        emotionGridContainer = new EmotionGridContainer(view.findViewById(R.id.emotion_selector));
        // 태그 입력
        tagBoxContainer = new TagBoxContainer(view.findViewById(R.id.tag_wrapper));
        // 점수 선택
        scoreContainer = new ScoreContainer(view.findViewById(R.id.score_wrapper));
        // 로딩 메시지
        loadingText = view.findViewById(R.id.loading_text);

        // 애니메이션 개체 묶음
        animationProvider = new AnimationProvider(view);

        // 스토리 자수 제한 감지
        storyContainer.setLimitObserver((Boolean isLimitExceeded) -> {
            setBottomButtonState(!isLimitExceeded);
        });

        // AI에게 부탁하기 버튼 감지
        storyContainer.observeAiButtonSwitch(isSet -> {
            if (isSet) {
                showLoadingText(true, R.string.ai_story_loading);
                setAiStoryCallSwitch();
                setBottomButtonState(false);
            }
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

    public void updateUiWithRemoteData(@NonNull StoryUiState storyUiState) {
        momentWriterContainer.setInvisible();

        if (storyUiState.isEmpty()) {
            return;
        }

        storyContainer.setUiWritingStory(storyUiState.getCreatedAt());
        storyContainer.setStoryText(storyUiState.getStoryTitle(), storyUiState.getStoryContent());
        storyContainer.freeze();
        storyContainer.setUiCompleteStory();

        emotionGridContainer.selectEmotion(storyUiState.getEmotion());
        emotionGridContainer.freeze();
        emotionWrapper.setVisibility(View.VISIBLE);

        tagBoxContainer.setTags(storyUiState.getTags());
        tagBoxContainer.freeze();
        tagBoxContainer.setUiVisible();

        scoreContainer.setScore(storyUiState.getScore());
        scoreContainer.setUiVisible();
    }

    public String getMomentInputText() {
        return momentWriterContainer.getInputText();
    }

    public String getStoryTitle() {
        return storyContainer.getStoryTitle();
    }

    public String getStoryContent() {
        return storyContainer.getStoryContent();
    }

    public int getSelectedEmotion() {
        return emotionGridContainer.getSelectedEmotion();
    }

    public String getTags() {
        return tagBoxContainer.getTags();
    }

    public void setAddButtonOnClickListener(View.OnClickListener listener) {
        momentWriterContainer.setAddButtonOnClickListener(listener);
    }

    public void setSubmitButtonOnClickListener(View.OnClickListener listener) {
        momentWriterContainer.setSubmitButtonOnClickListener(listener);
    }

    public void observeBottomButtonState(Observer<Boolean> observer) {
        bottomButtonState.observeForever(observer);
    }

    public void observeScrollToBottomSwitch(Observer<Boolean> observer) {
        scrollToBottomSwitch.observeForever(observer);
    }

    public void observeAiStoryCallSwitch(Observer<Boolean> observer) {
        aiStoryCallSwitch.observeForever(observer);
    }

    public void observeScore(Observer<Integer> observer) {
        scoreContainer.observeScore(observer);
    }

    public void freezeStoryEditText() {
        storyContainer.freeze();
    }

    public void freezeEmotionSelector() {
        emotionGridContainer.freeze();
    }

    public void freezeTagEditText() {
        tagBoxContainer.freeze();
    }

    public void setUiWritingMoment() {
        // add 누르고 입력창 뜨는 동작
        momentWriterContainer.setUiWritingMoment();

        setBottomButtonState(false);
        setScrollToBottomSwitch();
    }

    public void setUiReadyToAddMoment() {
        // submit 버튼 눌렀을 때 입력창 사라지고 add 버튼 표시되는 동작
        momentWriterContainer.setUiReadyToAddMoment();

        setBottomButtonState(true);
        setScrollToBottomSwitch();
    }

    public void setUiAddLimitExceeded() {
        // 모먼트 한 시간 2개 제한 초과했을 때
        momentWriterContainer.setUiAddLimitExceeded();

        setBottomButtonState(true);
        setScrollToBottomSwitch();
    }

    public void setUiWritingStory() {
        // 스토리 작성 칸 나올 때
        momentWriterContainer.setInvisible();

        storyContainer.setUiWritingStory();

        setBottomButtonState(true);
        setScrollToBottomSwitch();
    }

    public void setUiSelectingEmotion() {
        storyContainer.setUiCompleteStory();

        emotionWrapper.setVisibility(View.VISIBLE);
        emotionWrapper.startAnimation(animationProvider.fadeIn);

        setBottomButtonState(false);
        setScrollToBottomSwitch();
    }

    public void setUiWritingTags() {
        tagBoxContainer.setUiVisible();
        setScrollToBottomSwitch();
    }

    public void setUiSelectingScore() {
        scoreContainer.setUiVisible();
        setScrollToBottomSwitch();
    }

    public Observer<AiStoryState> aiStoryObserver() {
        return (AiStoryState aiStoryState) -> {
            showLoadingText(false);
            setBottomButtonState(true);

            if (aiStoryState.getError() != null) {
                Toast.makeText(view.getContext(), R.string.please_retry, Toast.LENGTH_SHORT)
                    .show();
                storyContainer.setAiButtonVisibility(View.VISIBLE);
                return;
            }
            storyContainer.setStoryText(aiStoryState.getTitle(), aiStoryState.getContent());
        };
    }

    public void showLoadingText(boolean on, @StringRes int textResId) {
        if (on) {
            loadingText.setText(textResId);
            loadingText.clearAnimation();
            loadingText.startAnimation(animationProvider.fadeInOut);
            loadingText.setVisibility(View.VISIBLE);
            setBottomButtonState(false);
        } else {
            loadingText.setVisibility(View.GONE);
            loadingText.clearAnimation();
            setBottomButtonState(true);
        }
    }

    public void showLoadingText(boolean on) {
        showLoadingText(on, R.string.completion_loading);
    }


    private void setBottomButtonState(boolean activated) {
        bottomButtonState.setValue(activated);
    }

    private void setScrollToBottomSwitch() {
        scrollToBottomSwitch.setValue(true);
        scrollToBottomSwitch.setValue(false);
    }

    private void setAiStoryCallSwitch() {
        aiStoryCallSwitch.setValue(true);
        aiStoryCallSwitch.setValue(false);
    }
}
