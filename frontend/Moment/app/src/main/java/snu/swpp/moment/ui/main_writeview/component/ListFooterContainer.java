package snu.swpp.moment.ui.main_writeview.component;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import snu.swpp.moment.R;
import snu.swpp.moment.ui.main_writeview.uistate.AiStoryState;
import snu.swpp.moment.ui.main_writeview.uistate.StoryUiState;
import snu.swpp.moment.utils.AnimationProvider;
import snu.swpp.moment.utils.EmotionMap;


public class ListFooterContainer {

    private final View view;
    private final LifecycleOwner lifecycleOwner;
    private final boolean isToday;  // for debugging

    // 현재 상태
    private WritePageState state;

    // 모먼트 작성
    private final MomentContainer momentContainer;
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
    // 마무리된 하루 텍스트
    private final TextView completedText;

    // 애니메이션 개체 묶음
    private final AnimationProvider animationProvider;

    // 하단 버튼 활성화 상태
    private final MutableLiveData<Boolean> bottomButtonState = new MutableLiveData<>(false);

    // 새 요소 추가 시 하단으로 스크롤 하기 위한 스위치
    private final MutableLiveData<Boolean> scrollToBottomSwitch = new MutableLiveData<>(false);
    // AI 요약 API call을 위한 스위치
    private final MutableLiveData<Boolean> aiStoryCallSwitch = new MutableLiveData<>(false);

    public ListFooterContainer(@NonNull View view, @NonNull LifecycleOwner lifecycleOwner,
        boolean isToday) {
        this.view = view;
        this.lifecycleOwner = lifecycleOwner;
        this.isToday = isToday;

        // 모먼트 작성
        momentContainer = new MomentContainer(view.findViewById(R.id.moment_writer));
        // 스토리 작성
        storyContainer = new StoryContainer(view.findViewById(R.id.story_wrapper));
        // 감정 선택
        emotionContainer = new EmotionContainer(view.findViewById(R.id.emotion_wrapper));
        // 태그 입력
        tagBoxContainer = new TagBoxContainer(view.findViewById(R.id.tag_wrapper));
        // 점수 선택
        scoreContainer = new ScoreContainer(view.findViewById(R.id.score_wrapper));
        // 로딩 메시지
        loadingText = view.findViewById(R.id.loading_text);

        // 애니메이션 개체 묶음
        animationProvider = new AnimationProvider(view);
        // 마무리된 하루 텍스트
        completedText = view.findViewById(R.id.completed_text);

        // 스토리 자수 제한 감지
        storyContainer.observeLimit(lifecycleOwner, isLimitExceeded -> {
            log("story observeLimit: " + isLimitExceeded);
            setBottomButtonState(!isLimitExceeded);
        });

        // AI에게 부탁하기 버튼 감지
        storyContainer.observeAiButtonSwitch(lifecycleOwner, isSet -> {
            log("observeAiButtonSwitch: " + isSet);
            if (isSet) {
                showLoadingText(true, R.string.ai_story_loading);
                setAiStoryCallSwitch();
            }
        });

        // 감정 선택 감지
        emotionContainer.observeSelectedEmotion((Integer emotion) -> {
            log("observeSelectedEmotion: " + emotion);
            if (state == WritePageState.EMOTION) {
                setBottomButtonState(-1 < emotion && emotion < EmotionMap.INVALID_EMOTION);
            }
        });

        // 태그 개수 제한 감지
        tagBoxContainer.observeLimit(lifecycleOwner, (Boolean isLimitExceeded) -> {
            log("tag observeLimit: " + isLimitExceeded);
            if (state == WritePageState.TAG) {
                setBottomButtonState(!isLimitExceeded);
            }
        });
    }

    public void updateWithServerData(@NonNull StoryUiState storyUiState, boolean isToday) {
        if (storyUiState.hasNoData()) {
            // 보여줄 데이터가 없는 경우
            if (isToday) {
                setState(WritePageState.MOMENT_READY_TO_ADD);
            } else {
                setState(WritePageState.FOOTER_INVISIBLE);
            }
        } else {
            setState(WritePageState.COMPLETE);
            storyContainer.setStoryText(storyUiState.getTitle(), storyUiState.getContent());
            storyContainer.setCompletedDate(storyUiState.getCreatedAt());
            emotionContainer.selectEmotion(storyUiState.getEmotion());
            tagBoxContainer.setTags(storyUiState.getTags());
            scoreContainer.setScore(storyUiState.getScore());

            String day = isToday ? "오늘" : "이날";
            emotionContainer.setHelpText(day + "의 감정");
            tagBoxContainer.setHelpText(day + "의 태그");
            scoreContainer.setHelpText(day + "의 점수");

            setScrollToBottomSwitch();
        }
    }

    public void setState(WritePageState state) {
        log(String.format("setState: %s -> %s", this.state, state));
        this.state = state;

        updateMomentContainer();
        updateStoryContainer();
        updateEmotionContainer();
        updateTagBoxContainer();
        updateScoreContainer();
        updateCompletedText();

        if (state != WritePageState.FOOTER_INVISIBLE) {
            setScrollToBottomSwitch();
        }
    }

    private void updateMomentContainer() {
        switch (state) {
            case MOMENT_READY_TO_ADD:
                momentContainer.setState(MomentContainerState.READY_TO_ADD);
                break;
            case MOMENT_WRITING:
                momentContainer.setState(MomentContainerState.WRITING);
                break;
            case MOMENT_WAITING_AI_REPLY:
                momentContainer.setState(MomentContainerState.WAITING_AI_REPLY);
                break;
            case MOMENT_ADD_LIMIT_EXCEEDED:
                momentContainer.setState(MomentContainerState.ADD_LIMIT_EXCEEDED);
                break;
            default:
                momentContainer.setState(MomentContainerState.INVISIBLE);
        }
    }

    private void updateStoryContainer() {
        switch (state) {
            case STORY:
                storyContainer.setState(StoryContainerState.WRITING);
                break;
            case EMOTION:
            case TAG:
            case SCORE:
            case COMPLETE:
                storyContainer.setState(StoryContainerState.COMPLETE);
                break;
            default:
                storyContainer.setState(StoryContainerState.INVISIBLE);
        }
    }

    private void updateEmotionContainer() {
        switch (state) {
            case EMOTION:
                emotionContainer.setState(EmotionContainerState.SELECTING);
                break;
            case TAG:
            case SCORE:
            case COMPLETE:
                emotionContainer.setState(EmotionContainerState.COMPLETE);
                break;
            default:
                emotionContainer.setState(EmotionContainerState.INVISIBLE);
        }
    }

    private void updateTagBoxContainer() {
        switch (state) {
            case TAG:
                tagBoxContainer.setState(TagBoxContainerState.WRITING);
                break;
            case SCORE:
            case COMPLETE:
                tagBoxContainer.setState(TagBoxContainerState.COMPLETE);
                break;
            default:
                tagBoxContainer.setState(TagBoxContainerState.INVISIBLE);
        }
    }

    private void updateScoreContainer() {
        switch (state) {
            case SCORE:
                scoreContainer.setState(ScoreContainerState.SELECTING);
                break;
            case COMPLETE:
                scoreContainer.setState(ScoreContainerState.COMPLETE);
                break;
            default:
                scoreContainer.setState(ScoreContainerState.INVISIBLE);
        }
    }

    private void updateCompletedText() {
        if (!isToday) {
            return;
        }
        switch (state) {
            case COMPLETE:
                completedText.setVisibility(View.VISIBLE);
                break;
            default:
                completedText.setVisibility(View.GONE);
        }
    }

    public String getMomentInput() {
        return momentContainer.getInputText();
    }

    public String[] getStoryInput() {
        return new String[]{
            storyContainer.getStoryTitle(),
            storyContainer.getStoryContent()
        };
    }

    public int getEmotionInput() {
        return emotionContainer.getSelectedEmotion();
    }

    public String getTagInput() {
        return tagBoxContainer.getTags();
    }

    public int getScoreInput() {
        return scoreContainer.getScore();
    }

    public boolean isCompletionInProgress() {
        return (state == WritePageState.STORY
            || state == WritePageState.EMOTION
            || state == WritePageState.TAG
            || state == WritePageState.SCORE);
    }

    public void setAddButtonOnClickListener(View.OnClickListener listener) {
        momentContainer.setAddButtonOnClickListener(listener);
    }

    public void setSubmitButtonOnClickListener(View.OnClickListener listener) {
        momentContainer.setSubmitButtonOnClickListener(listener);
    }

    public void observeBottomButtonState(Observer<Boolean> observer) {
        bottomButtonState.observe(lifecycleOwner, observer);
    }

    public void observeScrollToBottomSwitch(Observer<Boolean> observer) {
        scrollToBottomSwitch.observe(lifecycleOwner, observer);
    }

    public void observeAiStoryCallSwitch(Observer<Boolean> observer) {
        aiStoryCallSwitch.observe(lifecycleOwner, observer);
    }

    public void removeObservers() {
        bottomButtonState.removeObservers(lifecycleOwner);
        scrollToBottomSwitch.removeObservers(lifecycleOwner);
        aiStoryCallSwitch.removeObservers(lifecycleOwner);

        storyContainer.removeObservers(lifecycleOwner);
        emotionContainer.removeObservers(lifecycleOwner);
        tagBoxContainer.removeObservers(lifecycleOwner);
    }

    public Observer<AiStoryState> aiStoryObserver() {
        return (AiStoryState aiStoryState) -> {
            if (state != WritePageState.STORY) {
                return;
            }

            log("aiStoryObserver: " + aiStoryState);
            showLoadingText(false);
            setBottomButtonState(true);

            if (aiStoryState.getError() != null) {
                Toast.makeText(view.getContext(), R.string.please_retry, Toast.LENGTH_SHORT)
                    .show();
                storyContainer.setAiButtonVisibility(View.VISIBLE);
                return;
            }
            storyContainer.setStoryText(aiStoryState.getTitle(), aiStoryState.getContent());
            Toast.makeText(view.getContext(), R.string.ai_story_done, Toast.LENGTH_SHORT)
                .show();
        };
    }

    public void showLoadingText(boolean on) {
        showLoadingText(on, R.string.completion_loading);
    }

    private void showLoadingText(boolean on, @StringRes int textResId) {
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

    private void setBottomButtonState(boolean activated) {
        log("setBottomButtonState: " + activated);
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

    private void log(String msg) {
        String tag = "ListFooterContainer-" + (isToday ? "Today" : "Daily");
        Log.d(tag, msg);
    }
}
