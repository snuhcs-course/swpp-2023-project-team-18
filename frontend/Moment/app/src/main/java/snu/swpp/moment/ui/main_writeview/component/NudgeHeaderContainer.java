package snu.swpp.moment.ui.main_writeview.component;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import snu.swpp.moment.R;
import snu.swpp.moment.ui.main_writeview.uistate.NudgeUiState;
import snu.swpp.moment.utils.AnimationProvider;

public class NudgeHeaderContainer {

    private final ConstraintLayout nudgeWrapper;
    private final TextView nudgeHelpText;
    private final TextView nudgeText;
    private final Button deleteButton;

    private final AnimationProvider animationProvider;

    public NudgeHeaderContainer(@NonNull View view) {
        nudgeWrapper = view.findViewById(R.id.nudgeWrapper);
        nudgeHelpText = view.findViewById(R.id.nudgeHelpText);
        nudgeText = view.findViewById(R.id.nudgeText);
        deleteButton = view.findViewById(R.id.nudgeDeleteButton);

        animationProvider = new AnimationProvider(view);

        deleteButton.setOnClickListener(v -> {
            setVisibility(false);
            // TODO: Delete 되었음을 알리는 API 호출
        });
    }

    public void updateUi(NudgeUiState uiState) {
        if (uiState.isDeleted()) {
            setVisibility(false);
        } else {
            setVisibility(true);
            nudgeText.setText(uiState.getContent());
        }
    }

    private void setVisibility(boolean visible) {
        // TODO: GONE 처리 후에도 리스트 위쪽에 공간이 남아 있는 문제 해결
        if (!visible) {
            nudgeWrapper.startAnimation(animationProvider.longFadeOut);
            nudgeWrapper.postDelayed(() -> {
                nudgeWrapper.setVisibility(View.GONE);
                nudgeHelpText.setVisibility(View.GONE);
                nudgeText.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
            }, animationProvider.longFadeOut.getDuration());
        } else {
            nudgeWrapper.setVisibility(View.VISIBLE);
            nudgeHelpText.setVisibility(View.VISIBLE);
            nudgeText.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
        }
    }
}
