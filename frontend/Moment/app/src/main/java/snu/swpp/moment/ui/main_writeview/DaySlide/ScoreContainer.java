package snu.swpp.moment.ui.main_writeview.DaySlide;

import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import snu.swpp.moment.R;
import snu.swpp.moment.utils.AnimationProvider;

public class ScoreContainer {

    private final ConstraintLayout scoreWrapper;
    private final SeekBar scoreSeekBar;
    private final TextView scoreText;
    private final TextView autoCompleteWarnText;

    private final AnimationProvider animationProvider;

    private final MutableLiveData<Integer> score = new MutableLiveData<>();
    private final int DEFAULT_SCORE = 3;

    public ScoreContainer(@NonNull View view) {
        scoreWrapper = (ConstraintLayout) view;
        scoreSeekBar = view.findViewById(R.id.scoreSeekBar);
        scoreText = view.findViewById(R.id.scoreText);
        autoCompleteWarnText = view.findViewById(R.id.autoCompleteWarnText);
        animationProvider = new AnimationProvider(view);

        setScore(DEFAULT_SCORE);

        scoreSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int scoreValue, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                score.setValue(seekBar.getProgress());
                scoreText.setText(String.valueOf(getScore()));
                showAutoCompleteWarnText(false);
            }
        });
    }

    public int getScore() {
        Integer value = score.getValue();
        if (value == null) {
            return DEFAULT_SCORE;
        }
        return value;
    }

    public void setScore(int score) {
        this.score.setValue(score);
        scoreSeekBar.setProgress(score);
        scoreText.setText(String.valueOf(score));
    }

    public void setUiVisible() {
        scoreWrapper.setVisibility(View.VISIBLE);
        scoreWrapper.startAnimation(animationProvider.fadeIn);
    }

    public void showAutoCompleteWarnText(boolean visible) {
        if (visible) {
            autoCompleteWarnText.setVisibility(View.VISIBLE);
        } else {
            autoCompleteWarnText.setVisibility(View.GONE);
        }
    }

    public void observeScore(Observer<Integer> observer) {
        score.observeForever(observer);
    }
}
