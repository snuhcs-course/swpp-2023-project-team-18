package snu.swpp.moment.ui.main_writeview.DaySlide;

import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import snu.swpp.moment.R;

public class BottomButtonContainer {

    private final View view;
    private final Button bottomButton;

    public BottomButtonContainer(View view) {
        this.view = view;
        bottomButton = view.findViewById(R.id.bottom_button);
    }

    public void setActivated(boolean activated) {
        bottomButton.setActivated(activated);
    }

    public void dayCompletion() {
        bottomButton.setText(R.string.day_complete_string);
        bottomButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(),
                R.style.DialogTheme);
            builder.setMessage(R.string.day_complete_popup);
            builder.setPositiveButton(R.string.day_complete_popup_yes, (dialog, id) -> {
                // 네 -> 하루 마무리 시작
                // TODO
            });
            builder.setNegativeButton(R.string.day_complete_popup_no, (dialog, id) -> {
                // 아니요 -> Do nothing
            });
            builder.create().show();
        });
    }


}
