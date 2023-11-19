package snu.swpp.moment.ui.main_writeview.component;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import snu.swpp.moment.MainActivity;
import snu.swpp.moment.R;
import snu.swpp.moment.ui.main_writeview.uistate.CompletionState;
import snu.swpp.moment.ui.main_writeview.uistate.CompletionStoreResultState;
import snu.swpp.moment.ui.main_writeview.uistate.StoryUiState;
import snu.swpp.moment.ui.main_writeview.viewmodel.TodayViewModel;

public class BottomButtonContainer {

    private WritePageState state;

    private final MainActivity mainActivity;
    private final View view;
    private final Button button;
    private final TodayViewModel viewModel;
    private final ListFooterContainer listFooterContainer;

    public BottomButtonContainer(
        @NonNull MainActivity mainActivity,
        @NonNull View view,
        TodayViewModel viewModel,
        ListFooterContainer listFooterContainer
    ) {
        button = view.findViewById(R.id.bottomButton);

        this.mainActivity = mainActivity;
        this.view = view;
        this.viewModel = viewModel;
        this.listFooterContainer = listFooterContainer;

        this.listFooterContainer.observeBottomButtonState((Boolean state) -> {
            Log.d("BottomButtonContainer", "observeBottomButtonState: " + state);
            setActivated(state);
        });

        setState(WritePageState.FOOTER_INVISIBLE);
    }

    public void updateWithServerData(@NonNull StoryUiState storyUiState, boolean doMomentsExist) {
        if (storyUiState.hasNoData()) {
            setState(WritePageState.MOMENT_READY_TO_ADD, false);
            setActivated(doMomentsExist);
        } else {
            setState(WritePageState.COMPLETE, false);
        }
        listFooterContainer.updateWithServerData(storyUiState, true);
    }

    public void setState(WritePageState state) {
        setState(state, true);
    }

    private void setState(WritePageState state, boolean updateFooterState) {
        Log.d("BottomButtonContainer", String.format("setState: %s -> %s", this.state, state));
        this.state = state;

        if (updateFooterState) {
            listFooterContainer.setState(state);
        }
        updateButton();
        updateHamburgerButtonState();
    }

    private void updateButton() {
        switch (state) {
            default:
                button.setVisibility(View.VISIBLE);
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
                button.setVisibility(View.VISIBLE);
                button.setText(R.string.day_complete_story);
                button.setOnClickListener(v -> {
                    listFooterContainer.showLoadingText(true);
                    // 스토리 저장 API 호출
                    String[] story = listFooterContainer.getStoryInput();
                    viewModel.saveStory(story[0], story[1]);
                });
                break;
            case EMOTION:
                button.setVisibility(View.VISIBLE);
                button.setText(R.string.day_complete_emotion);
                button.setOnClickListener(v -> {
                    listFooterContainer.showLoadingText(true);
                    // 감정 저장 API 호출
                    viewModel.saveEmotion(listFooterContainer.getEmotionInput());
                });
                break;
            case TAG:
                button.setVisibility(View.VISIBLE);
                button.setText(R.string.day_completion_tag);
                button.setOnClickListener(v -> {
                    listFooterContainer.showLoadingText(true);
                    // 태그 저장 API 호출
                    viewModel.saveHashtags(listFooterContainer.getTagInput());
                });
                break;
            case SCORE:
                button.setVisibility(View.VISIBLE);
                button.setText(R.string.day_completion_score);
                button.setOnClickListener(v -> {
                    listFooterContainer.showLoadingText(true);

                    // 점수 저장 API 호출
                    viewModel.saveScore(listFooterContainer.getScoreInput());
                });
                break;
            case COMPLETE:
                button.setVisibility(View.GONE);
                setActivated(false);
                button.setText(R.string.day_complete_string);
                button.setOnClickListener(v -> {
                });
                break;
        }
    }

    private void updateHamburgerButtonState() {
        switch (state) {
            default:
                mainActivity.showHamburgerButton(true);
                break;
            case STORY:
            case EMOTION:
            case TAG:
            case SCORE:
                mainActivity.showHamburgerButton(false);
                break;
        }
    }

    public void setActivated(boolean activated) {
        Log.d("BottomButtonContainer", "setActivated: " + activated);
        button.setActivated(activated);
        button.setEnabled(activated);
    }

    public Observer<Boolean> waitingAiReplySwitchObserver() {
        // ListViewAdapter가 가지고 있는 LiveData에 등록해서 사용
        return (Boolean isWaitingAiReply) -> {
            Log.d("BottomButtonContainer", "waitingAiReplySwitchObserver: " + isWaitingAiReply);
            if (isWaitingAiReply) {
                setActivated(false);
                setState(WritePageState.MOMENT_WAITING_AI_REPLY);
            } else {
                setActivated(true);
                setState(WritePageState.MOMENT_READY_TO_ADD);
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

    public Observer<CompletionStoreResultState> scoreResultObserver() {
        return (CompletionStoreResultState completionStoreResultState) -> {
            Log.d("scoreResultObserver",
                "completionStoreResultState: " + completionStoreResultState.getError());
            listFooterContainer.showLoadingText(false);
            if (completionStoreResultState.getError() == null) {
                setState(WritePageState.COMPLETE);
            } else {
                Toast.makeText(view.getContext(), R.string.please_retry, Toast.LENGTH_SHORT)
                    .show();
                setActivated(true);
            }
        };
    }
}
