package snu.swpp.moment.ui.main_writeview.component;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import snu.swpp.moment.R;
import snu.swpp.moment.ui.main_writeview.uistate.NudgeUiState;
import snu.swpp.moment.utils.AnimationProvider;

public class NudgeHeaderContainer {

    private final ConstraintLayout nudgeWrapper;
    private final ConstraintLayout nudgeBox;
    private final TextView nudgeText;
    private final Button deleteButton;

    private final MutableLiveData<Boolean> deleteSwitch = new MutableLiveData<>(false);

    private final AnimationProvider animationProvider;

    public NudgeHeaderContainer(@NonNull View view) {
        nudgeWrapper = view.findViewById(R.id.nudgeWrapper);
        nudgeBox = view.findViewById(R.id.nudgeBox);
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
    public Observer<NudgeUiState> nudgeUiStateObserver(){
        return new Observer<NudgeUiState>() {
            @Override
            public void onChanged(NudgeUiState nudgeUiState) {
                Log.d("NudgeHeaderContainer", "Got nudge GET response: nudge="
                    + nudgeUiState.getContent());

                if(nudgeUiState == null)return;
                if(nudgeUiState.getError()!=null){
                    Toast.makeText(nudgeWrapper.getContext(),"넛지를 받아오지 못했어요",Toast.LENGTH_SHORT);
                    return;
                }
                updateUi(nudgeUiState);
            }
        };
    }

    private void setDeleteSwitch() {
        deleteSwitch.setValue(true);
        deleteSwitch.setValue(false);
    }


    public void observeDeleteSwitch(Observer<Boolean> observer) {
        deleteSwitch.observeForever(observer);
    }

}
