package snu.swpp.moment.ui.main_writeview.DaySlide;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import snu.swpp.moment.R;
import snu.swpp.moment.ui.main_writeview.TodayViewModel;
import snu.swpp.moment.ui.main_writeview.uistate.CompletionState;
import snu.swpp.moment.ui.main_writeview.uistate.CompletionStoreResultState;

public class BottomButtonContainer {

    private final Button button;
    private final View view;
    private final TodayViewModel viewModel;
    private final ListFooterContainer listFooterContainer;

    public BottomButtonContainer(@NonNull View view, TodayViewModel viewModel,
        ListFooterContainer listFooterContainer) {
        button = view.findViewById(R.id.bottomButton);

        this.view = view;
        this.viewModel = viewModel;
        this.listFooterContainer = listFooterContainer;

        this.listFooterContainer.observeBottomButtonState((Boolean state) -> {
            setActivated(state);
            Log.d("BottomButtonContainer", "setBottomButtonStateObserver: " + state);
        });
    }

    public void setActivated(boolean activated) {
        button.setActivated(activated);
        button.setEnabled(activated);
        Log.d("BottomButtonContainer", "setActivated: " + activated);
    }

    /* 모먼트 작성 중: 하루 마무리하기 버튼 */
    public void viewingMoment() {
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
    }

    /* 스토리 작성 중: 다음 단계로 이동 버튼 */
    public void writingStory() {
        listFooterContainer.setUiWritingStory();

        button.setText(R.string.next_stage_button);
        button.setOnClickListener(v -> {
            listFooterContainer.showLoadingText(true);
            // 스토리 저장 API 호출
            viewModel.saveStory(listFooterContainer.getStoryTitle(),
                listFooterContainer.getStoryContent());
        });
    }

    /* 감정 선택 중: 다음 단계로 이동 버튼 */
    public void selectingEmotion() {
        listFooterContainer.setUiSelectingEmotion();
        listFooterContainer.freezeStoryEditText();

        button.setText(R.string.next_stage_button);
        button.setOnClickListener(v -> {
            listFooterContainer.showLoadingText(true);
            // 감정 저장 API 호출
            viewModel.saveEmotion(listFooterContainer.getSelectedEmotion());
        });
    }

    public void writingTags() {
        listFooterContainer.setUiWritingTags();
        listFooterContainer.freezeEmotionSelector();

        button.setText(R.string.next_stage_button);
        button.setOnClickListener(v -> {
            listFooterContainer.showLoadingText(true);
            // 태그 저장 API 호출
            viewModel.saveHashtags(listFooterContainer.getTags());
        });
    }

    public void completionDone() {
        listFooterContainer.setUiSelectingScore();
        listFooterContainer.freezeTagEditText();

        button.setText(R.string.completed_day);
        setActivated(false);
    }

    public Observer<CompletionState> completionStateObserver() {
        return (CompletionState completionState) -> {
            if (completionState.getError() != null) {
                Toast.makeText(view.getContext(), R.string.please_retry, Toast.LENGTH_SHORT)
                    .show();
                return;
            }
            listFooterContainer.showLoadingText(false);
            writingStory();
        };
    }

    public Observer<CompletionStoreResultState> storyResultObserver() {
        return (CompletionStoreResultState completionStoreResultState) -> {
            if (completionStoreResultState.getError() != null) {
                Toast.makeText(view.getContext(), R.string.please_retry, Toast.LENGTH_SHORT)
                    .show();
                return;
            }
            listFooterContainer.showLoadingText(false);
            selectingEmotion();
        };
    }

    public Observer<CompletionStoreResultState> emotionResultObserver() {
        return (CompletionStoreResultState completionStoreResultState) -> {
            if (completionStoreResultState.getError() != null) {
                Toast.makeText(view.getContext(), R.string.please_retry, Toast.LENGTH_SHORT)
                    .show();
                return;
            }
            listFooterContainer.showLoadingText(false);
            writingTags();
        };
    }

    public Observer<CompletionStoreResultState> tagsResultObserver() {
        return (CompletionStoreResultState completionStoreResultState) -> {
            if (completionStoreResultState.getError() != null) {
                Toast.makeText(view.getContext(), R.string.please_retry, Toast.LENGTH_SHORT)
                    .show();
                return;
            }
            listFooterContainer.showLoadingText(false);
            completionDone();
        };
    }
}
