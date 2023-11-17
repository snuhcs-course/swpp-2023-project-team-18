package snu.swpp.moment.ui.main_writeview.component;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import snu.swpp.moment.R;
import snu.swpp.moment.ui.main_writeview.uistate.AiStoryState;
import snu.swpp.moment.ui.main_writeview.uistate.StoryUiState;
import snu.swpp.moment.utils.AnimationProvider;

public class ListFooterContainer {

    private final View view;
    private final boolean isToday;  // for debugging

    // 모먼트 작성
    private final MomentWriterContainer momentWriterContainer;
    // 스토리 작성
    private final StoryContainer storyContainer;
    // 감정 선택
    private final EmotionContainer emotionContainer;
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

    private enum ListFooterState {
        INVISIBLE,
        MOMENT_WRITING,
        MOMENT_WAITING_AI_REPLY,
        MOMENT_READY_TO_ADD,
        MOMENT_ADD_LIMIT_EXCEEDED,
        STORY_WRITING,
        EMOTION_SELECTING,
        TAG_WRITING,
        SCORE_SELECTING
    }

    private ListFooterState state = ListFooterState.INVISIBLE;


    public ListFooterContainer(@NonNull View view, boolean isToday) {
        this.view = view;
        this.isToday = isToday;

        // 모먼트 작성
        momentWriterContainer = new MomentWriterContainer(view.findViewById(R.id.moment_writer));
        // 스토리 작성
        storyContainer = new StoryContainer(view.findViewById(R.id.story_wrapper));
        // 감정 선택
        emotionContainer = new EmotionContainer(view.findViewById(R.id.emotion_selector));
        // 태그 입력
        tagBoxContainer = new TagBoxContainer(view.findViewById(R.id.tag_wrapper));
        // 점수 선택
        scoreContainer = new ScoreContainer(view.findViewById(R.id.score_wrapper));
        // 로딩 메시지
        loadingText = view.findViewById(R.id.loading_text);

        // 애니메이션 개체 묶음
        animationProvider = new AnimationProvider(view);

        // 스토리 자수 제한 감지
        storyContainer.observeLimit((Boolean isLimitExceeded) -> {
            log("observeLimit: " + isLimitExceeded);
            setBottomButtonState(!isLimitExceeded);
        });

        // AI에게 부탁하기 버튼 감지
        storyContainer.observeAiButtonSwitch(isSet -> {
            log("observeAiButtonSwitch: " + isSet);
            if (isSet) {
                showLoadingText(true, R.string.ai_story_loading);
                setAiStoryCallSwitch();
                setBottomButtonState(false);
            }
        });

        // 감정 선택 감지
        emotionContainer.observeSelectedEmotion((Integer emotion) -> {
            log("observeSelectedEmotion: " + emotion);
            setBottomButtonState(emotion > -1);
        });

        // 태그 개수 제한 감지
        tagBoxContainer.observeLimit((Boolean isLimitExceeded) -> {
            log("observeLimit: " + isLimitExceeded);
            setBottomButtonState(!isLimitExceeded);
        });
    }

    public void updateUiWithRemoteData(@NonNull StoryUiState storyUiState, boolean isToday) {
        if (storyUiState.hasNoData()) {
            // 보여줄 데이터가 없는 경우
            storyContainer.setState(StoryContainerState.INVISIBLE);
            emotionContainer.setState(EmotionContainerState.INVISIBLE);
//            storyContainer.resetUi();
//            emotionContainer.resetUi();
            tagBoxContainer.resetUi();
            scoreContainer.resetUi();

            if (isToday) {
                setUiReadyToAddMoment(false);
            } else {
                momentWriterContainer.setState(MomentWriterContainerState.INVISIBLE);
//                momentWriterContainer.setInvisible();
                state = ListFooterState.INVISIBLE;
            }
            return;
        }

        momentWriterContainer.setState(MomentWriterContainerState.INVISIBLE);
//        momentWriterContainer.setInvisible();

        storyContainer.setState(StoryContainerState.COMPLETE);
        storyContainer.setStoryText(storyUiState.getTitle(), storyUiState.getContent());
        storyContainer.setCompletedDate(storyUiState.getCreatedAt());
//        freezeStoryEditText();
//        storyContainer.setUiWritingStory(storyUiState.getCreatedAt());
//        storyContainer.setStoryText(storyUiState.getTitle(), storyUiState.getContent());
//        storyContainer.setUiCompleteStory();

        emotionContainer.setState(EmotionContainerState.COMPLETE);
        emotionContainer.selectEmotion(storyUiState.getEmotion());
//        freezeEmotionSelector();
//        emotionContainer.selectEmotion(storyUiState.getEmotion());
//        emotionContainer.setUiVisible();

        freezeTagEditText();
        tagBoxContainer.setTags(storyUiState.getTags());
        tagBoxContainer.setUiVisible();

        scoreContainer.setScore(storyUiState.getScore());
        scoreContainer.setUiVisible();
        scoreContainer.showAutoCompleteWarnText(!storyUiState.isPointCompleted());

        setHelpTextAfterCompleted(isToday);
        state = ListFooterState.SCORE_SELECTING;
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
        return emotionContainer.getSelectedEmotion();
    }

    public int getScore() {
        return scoreContainer.getScore();
    }

    public String getTags() {
        return tagBoxContainer.getTags();
    }

    public boolean isCompletionInProgress() {
        log("isCompletionInProgress: " + state.name());
        return (state == ListFooterState.STORY_WRITING ||
            state == ListFooterState.EMOTION_SELECTING ||
            state == ListFooterState.TAG_WRITING);
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

    public void observeSaveScoreSwitch(Observer<Boolean> observer) {
        scoreContainer.observeSaveScoreSwitch(observer);
    }

    public void freezeStoryEditText() {
        storyContainer.freeze(true);
    }

    public void freezeEmotionSelector() {
        emotionContainer.freeze(true);
    }

    public void freezeTagEditText() {
        tagBoxContainer.freeze(true);
    }

    public void setUiWritingMoment() {
        // add 누르고 입력창 뜨는 동작
        log("[STATE] setUiWritingMoment");
        momentWriterContainer.setState(MomentWriterContainerState.WRITING);
//        momentWriterContainer.setUiWritingMoment();

        setBottomButtonState(false);
        setScrollToBottomSwitch();
        state = ListFooterState.MOMENT_WRITING;
    }

    public void setUiWaitingAiReply() {
        // submit 누른 후 AI 답글 대기 중일 때
        log("[STATE] setUiWaitingAiReply");
        momentWriterContainer.setState(MomentWriterContainerState.WAITING_AI_REPLY);
//        momentWriterContainer.setUiWaitingAiReply();

        setBottomButtonState(false);
        state = ListFooterState.MOMENT_WAITING_AI_REPLY;
    }

    public void setUiReadyToAddMoment(boolean activateBottomButton) {
        // submit 버튼 눌렀을 때 입력창 사라지고 add 버튼 표시되는 동작
        log("[STATE] setUiReadyToAddMoment - " + activateBottomButton);
        momentWriterContainer.setState(MomentWriterContainerState.READY_TO_ADD);
//        momentWriterContainer.setUiReadyToAddMoment();

        if (activateBottomButton) {
            setBottomButtonState(true);
        }
        setScrollToBottomSwitch();
        state = ListFooterState.MOMENT_READY_TO_ADD;
    }

    public void setUiAddLimitExceeded() {
        // 모먼트 한 시간 2개 제한 초과했을 때
        log("[STATE] setUiAddLimitExceeded");
        momentWriterContainer.setState(MomentWriterContainerState.ADD_LIMIT_EXCEEDED);
//        momentWriterContainer.setUiAddLimitExceeded();

        setBottomButtonState(true);
        setScrollToBottomSwitch();
        state = ListFooterState.MOMENT_ADD_LIMIT_EXCEEDED;
    }

    public void setUiWritingStory() {
        // 스토리 작성 칸 나올 때
        log("[STATE] setUiWritingStory");
        momentWriterContainer.setState(MomentWriterContainerState.INVISIBLE);
//        momentWriterContainer.setInvisible();

        storyContainer.setState(StoryContainerState.WRITING);
//        storyContainer.setUiWritingStory();

        setBottomButtonState(true);
        setScrollToBottomSwitch();
        state = ListFooterState.STORY_WRITING;
    }

    public void setUiSelectingEmotion() {
        log("[STATE] setUiSelectingEmotion");
        storyContainer.setState(StoryContainerState.COMPLETE);
        emotionContainer.setState(EmotionContainerState.SELECTING);
//        storyContainer.setUiCompleteStory();
//        emotionContainer.setUiSelectingEmotion();

        setBottomButtonState(false);
        setScrollToBottomSwitch();
        state = ListFooterState.EMOTION_SELECTING;
    }

    public void setUiWritingTags() {
        log("[STATE] setUiWritingTags");
        tagBoxContainer.setUiVisible();
        setScrollToBottomSwitch();
        state = ListFooterState.TAG_WRITING;
    }

    public void setUiSelectingScore() {
        log("[STATE] setUiSelectingScore");
        scoreContainer.setUiVisible();
        setScrollToBottomSwitch();
        state = ListFooterState.SCORE_SELECTING;
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
        log("showLoadingText: " + on);
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

    private void setHelpTextAfterCompleted(boolean isToday) {
        String day = isToday ? "오늘" : "이날";
        emotionContainer.setHelpText(day + "의 감정");
        tagBoxContainer.setHelpText(day + "의 태그");
        scoreContainer.setHelpText(day + "의 점수");
    }

    private void log(String msg) {
        String tag = "ListFooterContainer-" + (isToday ? "Today" : "Daily");
        Log.d(tag, msg);
    }
}
