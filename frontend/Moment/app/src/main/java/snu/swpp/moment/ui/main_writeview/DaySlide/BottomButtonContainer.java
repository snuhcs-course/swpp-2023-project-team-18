package snu.swpp.moment.ui.main_writeview.DaySlide;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import java.util.Date;
import snu.swpp.moment.R;

public class BottomButtonContainer {

    private final Button button;
    private final View view;
    private final ListFooterContainer listFooterContainer;

    public BottomButtonContainer(@NonNull View view, ListFooterContainer listFooterContainer) {
        button = view.findViewById(R.id.bottomButton);

        this.view = view;
        this.listFooterContainer = listFooterContainer;

        this.listFooterContainer.setBottomButtonStateObserver((Boolean state) -> {
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
                Date completeTime = new Date();
                writingStory(completeTime);
                // TODO: API 호출해서 마무리 시점 기록
            });
            builder.setNegativeButton(R.string.popup_no, (dialog, id) -> {
            });
            builder.create().show();
        });
    }

    /* 스토리 작성 중: 다음 단계로 이동 버튼 */
    public void writingStory(Date completeTime) {
        button.setText(R.string.next_stage_button);
        button.setOnClickListener(v -> {
            // TODO: 스토리 저장 API 호출
            selectingEmotion();
        });

        listFooterContainer.setUiWritingStory(completeTime);
    }

    /* 감정 선택 중: 다음 단계로 이동 버튼 */
    public void selectingEmotion() {
        button.setText(R.string.next_stage_button);
        button.setOnClickListener(v -> {
            // TODO: 감정 저장 API 호출
            writingTags();
        });

        listFooterContainer.setUiSelectingEmotion();
        listFooterContainer.freezeStoryEditText();
    }

    public void writingTags() {
        button.setText(R.string.next_stage_button);
        button.setOnClickListener(v -> {
            // TODO: 태그 저장 API 호출
            completionDone();
        });

        listFooterContainer.setUiWritingTags();
        listFooterContainer.freezeEmotionSelector();
    }

    public void completionDone() {
        button.setText(R.string.completed_day);
        setActivated(false);

        listFooterContainer.setUiSelectingScore();
        listFooterContainer.freezeTagEditText();
    }
}
