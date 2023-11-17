package snu.swpp.moment.ui.main_writeview.component;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import snu.swpp.moment.R;
import snu.swpp.moment.ui.main_writeview.uistate.CompletionState;
import snu.swpp.moment.ui.main_writeview.uistate.CompletionStoreResultState;
import snu.swpp.moment.ui.main_writeview.viewmodel.TodayViewModel;

public class BottomButtonContainer {

    private WritePageState state;

    private final Button button;
    private final View view;
    private final TodayViewModel viewModel;
    private final ListFooterContainerNew listFooterContainer;

    public BottomButtonContainer(@NonNull View view, TodayViewModel viewModel,
        ListFooterContainerNew listFooterContainer) {
        button = view.findViewById(R.id.bottomButton);

        this.view = view;
        this.viewModel = viewModel;
        this.listFooterContainer = listFooterContainer;

        this.listFooterContainer.observeBottomButtonState((Boolean state) -> {
            Log.d("BottomButtonContainer", "observeBottomButtonState: " + state);
            setActivated(state, false);
        });

        setState(WritePageState.INVISIBLE);
    }

    public void setState(WritePageState state) {
        Log.d("BottomButtonContainer", String.format("setState: %s -> %s", this.state, state));
        this.state = state;

        listFooterContainer.setState(state);
        updateButton();
    }

    private void updateButton() {
        switch (state) {
            case INVISIBLE:
                button.setText(R.string.day_complete_string);
                button.setOnClickListener(v -> {
                    // Popup dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(),
                        R.style.DialogTheme);
                    builder.setMessage(R.string.day_complete_popup);

                    builder.setPositiveButton(R.string.popup_yes, (dialog, id) -> {
                        // 네 -> 하루 마무리 시작
                        listFooterContainer.showLoadingText(true);
                        // 마무리 상태 기록 API 호출
                        viewModel.notifyCompletion();
                    });
                    builder.setNegativeButton(R.string.popup_no, (dialog, id) -> {
                    });
                    builder.create().show();
                });
                break;
            case STORY:
                button.setText(R.string.day_complete_story);
                button.setOnClickListener(v -> {
                    listFooterContainer.showLoadingText(true);
                    // 스토리 저장 API 호출
                    String[] story = listFooterContainer.getStoryInput();
                    viewModel.saveStory(story[0], story[1]);
                });
                break;
            case EMOTION:
                button.setText(R.string.day_complete_emotion);
                button.setOnClickListener(v -> {
                    listFooterContainer.showLoadingText(true);
                    // 감정 저장 API 호출
                    viewModel.saveEmotion(listFooterContainer.getEmotionInput());
                });
                break;
            case TAG:
                button.setText(R.string.day_completion_tag);
                button.setOnClickListener(v -> {
                    listFooterContainer.showLoadingText(true);
                    // 태그 저장 API 호출
                    viewModel.saveHashtags(listFooterContainer.getTagInput());
                });
                break;
            case SCORE:
                button.setText(R.string.day_completion_score);
                button.setOnClickListener(v -> {
                    listFooterContainer.showLoadingText(true);
                    // 점수 저장 API 호출
                    viewModel.saveScore(listFooterContainer.getScoreInput());
                });
                break;
            case COMPLETE:
                button.setVisibility(View.GONE);
                button.setText(R.string.day_complete_string);
                button.setOnClickListener(v -> {
                });
                break;
        }
    }

    public void setActivated(boolean activated, boolean completed) {
        Log.d("BottomButtonContainer", "setActivated: " + activated + ", " + completed);
        setActivated(activated);
        button.setText(completed ? R.string.completed_day : R.string.day_complete_string);
    }

    public void setActivated(boolean activated) {
        Log.d("BottomButtonContainer", "setActivated: " + activated);
        button.setActivated(activated);
        button.setEnabled(activated);
    }

    /* 스토리 작성 중: 다음 단계로 이동 버튼 */
//    private void writingStory() {
//        listFooterContainer.setUiWritingStory();
//
//        button.setText(R.string.next_stage_button);
//        button.setOnClickListener(v -> {
//            listFooterContainer.showLoadingText(true);
//            // 스토리 저장 API 호출
//            viewModel.saveStory(listFooterContainer.getStoryTitle(),
//                listFooterContainer.getStoryContent());
//        });
//    }

    /* 감정 선택 중: 다음 단계로 이동 버튼 */
//    private void selectingEmotion() {
//        listFooterContainer.setUiSelectingEmotion();
//        listFooterContainer.freezeStoryEditText();
//
//        button.setText(R.string.next_stage_button);
//        button.setOnClickListener(v -> {
//            listFooterContainer.showLoadingText(true);
//            // 감정 저장 API 호출
//            viewModel.saveEmotion(listFooterContainer.getSelectedEmotion());
//        });
//    }

//    private void writingTags() {
//        listFooterContainer.setUiWritingTags();
//        listFooterContainer.freezeEmotionSelector();
//
//        button.setText(R.string.next_stage_button);
//        button.setOnClickListener(v -> {
//            listFooterContainer.showLoadingText(true);
//            // 태그 저장 API 호출
//            viewModel.saveHashtags(listFooterContainer.getTags());
//        });
//    }

//    private void completionDone() {
//        listFooterContainer.setUiSelectingScore();
//        listFooterContainer.freezeTagEditText();
//
//        setActivated(false, true);
//    }

    public Observer<Boolean> waitingAiReplySwitchObserver() {
        // ListViewAdapter가 가지고 있는 LiveData에 등록해서 사용
        return (Boolean isWaitingAiReply) -> {
            Log.d("BottomButtonContainer", "waitingAiReplySwitchObserver: " + isWaitingAiReply);
            if (isWaitingAiReply) {
                setActivated(false);
                listFooterContainer.setState(WritePageState.MOMENT_WAITING_AI_REPLY);
            } else {
                setActivated(true);
                listFooterContainer.setState(WritePageState.MOMENT_READY_TO_ADD);
            }
        };
    }

    public Observer<CompletionState> completionStateObserver() {
        return (CompletionState completionState) -> {
            Log.d("BottomButtonContainer",
                "completionStateObserver: " + completionState.getError());
            listFooterContainer.showLoadingText(false);
            if (completionState.getError() == null) {
                setState(WritePageState.STORY);
            } else {
                Toast.makeText(view.getContext(), R.string.please_retry, Toast.LENGTH_SHORT)
                    .show();
                setActivated(true);
            }
        };
    }

    public Observer<CompletionStoreResultState> storyResultObserver() {
        return (CompletionStoreResultState completionStoreResultState) -> {
            Log.d("BottomButtonContainer",
                "completionStoreResultState: " + completionStoreResultState.getError());
            listFooterContainer.showLoadingText(false);
            if (completionStoreResultState.getError() == null) {
                setState(WritePageState.EMOTION);
            } else {
                Toast.makeText(view.getContext(), R.string.please_retry, Toast.LENGTH_SHORT)
                    .show();
                setActivated(true);
            }
        };
    }

    public Observer<CompletionStoreResultState> emotionResultObserver() {
        return (CompletionStoreResultState completionStoreResultState) -> {
            Log.d("emotionResultObserver",
                "completionStoreResultState: " + completionStoreResultState.getError());
            listFooterContainer.showLoadingText(false);
            if (completionStoreResultState.getError() == null) {
                setState(WritePageState.TAG);
            } else {
                Toast.makeText(view.getContext(), R.string.please_retry, Toast.LENGTH_SHORT)
                    .show();
                setActivated(true);
            }
        };
    }

    public Observer<CompletionStoreResultState> tagsResultObserver() {
        return (CompletionStoreResultState completionStoreResultState) -> {
            Log.d("tagsResultObserver",
                "completionStoreResultState: " + completionStoreResultState.getError());
            listFooterContainer.showLoadingText(false);
            if (completionStoreResultState.getError() == null) {
                setState(WritePageState.SCORE);
            } else {
                Toast.makeText(view.getContext(), R.string.please_retry, Toast.LENGTH_SHORT)
                    .show();
                setActivated(true);
            }
        };
    }
}
