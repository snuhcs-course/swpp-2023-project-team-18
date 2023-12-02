package snu.swpp.moment.ui.main_writeview.component;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import snu.swpp.moment.LoginRegisterActivity;
import snu.swpp.moment.R;
import snu.swpp.moment.exception.UnauthorizedAccessException;
import snu.swpp.moment.ui.main_writeview.uistate.NudgeUiState;
import snu.swpp.moment.utils.AnimationProvider;

public class NudgeHeaderContainer {

    private final ConstraintLayout nudgeWrapper;
    private final ConstraintLayout nudgeBox;
    private final TextView nudgeText;
    private final Button deleteButton;


    private final AnimationProvider animationProvider;

    public NudgeHeaderContainer(@NonNull View view) {
        nudgeWrapper = view.findViewById(R.id.nudgeWrapper);
        nudgeBox = view.findViewById(R.id.nudgeBox);
        nudgeText = view.findViewById(R.id.nudgeText);
        deleteButton = view.findViewById(R.id.nudgeDeleteButton);

        animationProvider = new AnimationProvider(view);


    }

    private void updateUi(NudgeUiState uiState) {
        if (uiState.isDeleted()) {
            setVisibility(false);
        } else {
            setVisibility(true);
            nudgeText.setText(uiState.getContent());
        }
    }

    private void setVisibility(boolean visible) {
        if (!visible) {
            nudgeBox.startAnimation(animationProvider.longFadeOut);
            nudgeBox.postDelayed(() -> {
                nudgeWrapper.setVisibility(View.GONE);
                nudgeBox.setVisibility(View.GONE);
                nudgeText.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
            }, animationProvider.longFadeOut.getDuration());
        } else {
            nudgeWrapper.setVisibility(View.VISIBLE);
            nudgeBox.setVisibility(View.VISIBLE);
            nudgeText.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
        }
    }

    public Observer<NudgeUiState> nudgeUiStateObserver() {
        return new Observer<NudgeUiState>() {
            @Override
            public void onChanged(NudgeUiState nudgeUiState) {
                Log.d("NudgeHeaderContainer", "Got nudge response: nudge="
                    + nudgeUiState.getContent());

                if (nudgeUiState.getError() != null) {
                    if (nudgeUiState.getError() instanceof UnauthorizedAccessException) {
                        Intent intent = new Intent(nudgeWrapper.getContext(),
                            LoginRegisterActivity.class);
                        startActivity(nudgeWrapper.getContext(), intent, null);
                    } else {
                        return;
                    }
                }
                updateUi(nudgeUiState);
            }
        };
    }


    public void setOnDeleteButtonClickedListener(View.OnClickListener listener) {
        deleteButton.setOnClickListener(listener);
    }

}
