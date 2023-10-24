package snu.swpp.moment.ui.main_writeview.DaySlide;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import snu.swpp.moment.R;

public class BottomButtonContainer {

    private final View view;
    private final Button button;
    private final MutableLiveData<ButtonState> buttonState = new MutableLiveData<>();

    public enum ButtonState {
        VIEWING_MOMENT,
        WRITING_STORY,
        SELECTING_EMOTION,
    }

    public BottomButtonContainer(@NonNull View view) {
        this.view = view;
        button = view.findViewById(R.id.bottom_button);
    }

    public void setActivated(boolean activated) {
        button.setActivated(activated);
        button.setEnabled(activated);
        Log.d("BottomButtonContainer", "setActivated: " + activated);
    }

    public void setStateObserver(Observer<ButtonState> observer) {
        buttonState.observeForever(observer);
    }

    /* 모먼트 작성 중: 하루 마무리하기 버튼 */
    public void writingMoment() {
        buttonState.setValue(ButtonState.VIEWING_MOMENT);
        button.setText(R.string.day_complete_string);
        button.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(),
                R.style.DialogTheme);
            builder.setMessage(R.string.day_complete_popup);
            builder.setPositiveButton(R.string.day_complete_popup_yes, (dialog, id) -> {
                // 네 -> 하루 마무리 시작
                writingStory();
                // TODO: API 호출해서 마무리 시점 기록
            });
            builder.setNegativeButton(R.string.day_complete_popup_no, (dialog, id) -> {
            });
            builder.create().show();
        });
    }

    /* 스토리 작성 중: 다음 단계로 이동 버튼 */
    public void writingStory() {
        buttonState.setValue(ButtonState.WRITING_STORY);
        button.setText(R.string.next_stage_button);
        button.setOnClickListener(v -> {
            // TODO: 작성한 내용 검사 (아무것도 안 썼어도 통과인가?)
            //  스토리 저장 API 호출
            selectingEmotion();
        });
    }

    /* 감정 선택 중: 다음 단계로 이동 버튼 */
    public void selectingEmotion() {
        buttonState.setValue(ButtonState.SELECTING_EMOTION);
        button.setText(R.string.next_stage_button);
        button.setOnClickListener(v -> {
            // TODO: 감정 검사 (아무것도 선택 안 한 경우 block)
        });
    }
}
