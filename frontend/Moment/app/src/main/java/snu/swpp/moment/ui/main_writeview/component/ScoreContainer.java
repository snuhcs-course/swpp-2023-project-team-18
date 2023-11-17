package snu.swpp.moment.ui.main_writeview.component;

import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import snu.swpp.moment.R;
import snu.swpp.moment.utils.AnimationProvider;


enum ScoreContainerState {
    INVISIBLE,
    SELECTING,
    COMPLETE,
}


public class ScoreContainer {

    private ScoreContainerState state;

    private final ConstraintLayout scoreWrapper;
    private final SeekBar scoreSeekBar;
    private final TextView scoreHelpText;
    private final TextView scoreText;
    private final TextView autoCompleteWarnText;

    private final AnimationProvider animationProvider;

    private int score = -1;
    private final MutableLiveData<Boolean> saveScoreSwitch = new MutableLiveData<>(false);
    private static final int DEFAULT_SCORE = 3;

    public ScoreContainer(@NonNull View view) {
        scoreWrapper = (ConstraintLayout) view;
        scoreSeekBar = view.findViewById(R.id.scoreSeekBar);
        scoreHelpText = view.findViewById(R.id.scoreHelpText);
        scoreText = view.findViewById(R.id.scoreText);
        autoCompleteWarnText = view.findViewById(R.id.autoCompleteWarnText);
        animationProvider = new AnimationProvider(view);

        scoreSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int scoreValue, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setScore(seekBar.getProgress());
                showAutoCompleteWarnText(false);
                setSaveScoreSwitch();
            }
        });

        setScore(DEFAULT_SCORE);
    }

    public void setState(ScoreContainerState state) {
        Log.d("ScoreContainer", String.format("setState: %s -> %s", this.state, state));
        this.state = state;

        updateScoreWrapper();
        updateScoreSeekBar();
    }

    private void updateScoreWrapper() {
        switch (state) {
            case INVISIBLE:
                scoreWrapper.setVisibility(View.GONE);
                break;
            case SELECTING:
                scoreWrapper.setVisibility(View.VISIBLE);
                scoreWrapper.startAnimation(animationProvider.fadeIn);
                break;
            case COMPLETE:
                scoreWrapper.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updateScoreSeekBar() {
        switch (state) {
            case INVISIBLE:
            case COMPLETE:
                scoreSeekBar.setEnabled(false);
                break;
            case SELECTING:
                scoreSeekBar.setEnabled(true);
                break;
        }
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        scoreText.setText(String.valueOf(score));
        scoreSeekBar.setProgress(score);
    }

//    public void resetUi() {
//        scoreWrapper.setVisibility(View.GONE);
//        setScore(DEFAULT_SCORE);
//        scoreHelpText.setText(R.string.score_help_text);
//        showAutoCompleteWarnText(false);
//    }

//    public void setUiVisible() {
//        scoreWrapper.setVisibility(View.VISIBLE);
//        scoreWrapper.startAnimation(animationProvider.fadeIn);
//    }

    public void setHelpText(String text) {
        scoreHelpText.setText(text);
    }

    public void showAutoCompleteWarnText(boolean visible) {
        if (visible) {
            autoCompleteWarnText.setVisibility(View.VISIBLE);
        } else {
            autoCompleteWarnText.setVisibility(View.GONE);
        }
    }

    private void setSaveScoreSwitch() {
        saveScoreSwitch.setValue(true);
        saveScoreSwitch.setValue(false);
    }

    public void observeSaveScoreSwitch(Observer<Boolean> observer) {
        saveScoreSwitch.observeForever(observer);
    }

    public void removeObservers(LifecycleOwner lifecycleOwner) {
        saveScoreSwitch.removeObservers(lifecycleOwner);
    }
}
