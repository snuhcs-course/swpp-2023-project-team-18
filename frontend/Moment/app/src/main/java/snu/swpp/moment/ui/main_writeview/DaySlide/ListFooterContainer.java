package snu.swpp.moment.ui.main_writeview.DaySlide;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import java.util.Date;
import snu.swpp.moment.R;
import snu.swpp.moment.ui.main_writeview.EmotionGridContainer;
import snu.swpp.moment.utils.AnimationProvider;

public class ListFooterContainer {

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


    public ListFooterContainer(@NonNull View view) {
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

    public void setAddButtonOnClickListener(View.OnClickListener listener) {
        momentWriterContainer.setAddButtonOnClickListener(listener);
    }

    public void setSubmitButtonOnClickListener(View.OnClickListener listener) {
        momentWriterContainer.setSubmitButtonOnClickListener(listener);
    }

    public void setBottomButtonStateObserver(Observer<Boolean> observer) {
        bottomButtonState.observeForever(observer);
    }

    public void setScrollToBottomSwitchObserver(Observer<Boolean> observer) {
        scrollToBottomSwitch.observeForever(observer);
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

    public void setUiWritingStory(Date completeTime) {
        // 스토리 작성 칸 나올 때
        momentWriterContainer.setInvisible();

        storyContainer.setUiWritingStory(completeTime);

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

    public void showLoadingText(boolean on) {
        if (on) {
            loadingText.clearAnimation();
            loadingText.startAnimation(animationProvider.fadeInOut);
            loadingText.setVisibility(View.VISIBLE);
        } else {
            loadingText.setVisibility(View.GONE);
            loadingText.clearAnimation();
        }
    }


    private void setBottomButtonState(boolean activated) {
        bottomButtonState.setValue(activated);
    }

    private void setScrollToBottomSwitch() {
        scrollToBottomSwitch.setValue(true);
        scrollToBottomSwitch.setValue(false);
    }
}
