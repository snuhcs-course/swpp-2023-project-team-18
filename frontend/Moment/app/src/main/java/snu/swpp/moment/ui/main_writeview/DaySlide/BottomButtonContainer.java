package snu.swpp.moment.ui.main_writeview.DaySlide;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import snu.swpp.moment.R;
import snu.swpp.moment.ui.main_writeview.uistate.CompletionState;
import snu.swpp.moment.ui.main_writeview.uistate.CompletionStoreResultState;

public class BottomButtonContainer {

    private final Button button;
    private final View view;
    private final ListFooterContainer listFooterContainer;

    public BottomButtonContainer(@NonNull View view, ListFooterContainer listFooterContainer) {
        button = view.findViewById(R.id.bottomButton);

        this.view = view;
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
                // TODO: 마무리 상태 기록 API 호출
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
            // TODO: 스토리 저장 API 호출
        });
    }

    /* 감정 선택 중: 다음 단계로 이동 버튼 */
    public void selectingEmotion() {
        listFooterContainer.setUiSelectingEmotion();
        listFooterContainer.freezeStoryEditText();

        button.setText(R.string.next_stage_button);
        button.setOnClickListener(v -> {
            listFooterContainer.showLoadingText(true);
            // TODO: 감정 저장 API 호출
        });
    }

    public void writingTags() {
        listFooterContainer.setUiWritingTags();
        listFooterContainer.freezeEmotionSelector();

        button.setText(R.string.next_stage_button);
        button.setOnClickListener(v -> {
            listFooterContainer.showLoadingText(true);
            // TODO: 태그 저장 API 호출
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
            listFooterContainer.showLoadingText(false);
            writingStory();
        };
    }

    public Observer<CompletionStoreResultState> storyResultObserver() {
        return (CompletionStoreResultState completionStoreResultState) -> {
            listFooterContainer.showLoadingText(false);
            selectingEmotion();
        };
    }

    public Observer<CompletionStoreResultState> emotionResultObserver() {
        return (CompletionStoreResultState completionStoreResultState) -> {
            listFooterContainer.showLoadingText(false);
            writingTags();
        };
    }

    public Observer<CompletionStoreResultState> tagsResultObserver() {
        return (CompletionStoreResultState completionStoreResultState) -> {
            listFooterContainer.showLoadingText(false);
            completionDone();
        };
    }
}
