package snu.swpp.moment.ui.main_writeview.DaySlide;

import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import snu.swpp.moment.R;

public class BottomButtonContainer {

    private final View view;
    private final ListFooterContainer listFooterContainer;
    private final Button bottomButton;

    public BottomButtonContainer(View view, ListFooterContainer listFooterContainer) {
        this.view = view;
        this.listFooterContainer = listFooterContainer;
        bottomButton = view.findViewById(R.id.bottom_button);
    }

    public void setActivated(boolean activated) {
        bottomButton.setActivated(activated);
    }

    /* 모먼트 작성 중: 하루 마무리하기 버튼 */
    public void writingMoment() {
        bottomButton.setText(R.string.day_complete_string);
        bottomButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(),
                R.style.DialogTheme);
            builder.setMessage(R.string.day_complete_popup);
            builder.setPositiveButton(R.string.day_complete_popup_yes, (dialog, id) -> {
                // 네 -> 하루 마무리 시작
                listFooterContainer.setUiWritingStory();
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
        bottomButton.setText(R.string.next_stage_button);
        bottomButton.setOnClickListener(v -> {

        });
    }
}
