package snu.swpp.moment.ui.main_writeview.DaySlide;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import snu.swpp.moment.R;
import snu.swpp.moment.utils.AnimationProvider;

public class ScoreContainer {

    private final ConstraintLayout scoreWrapper;
    private final SeekBar scoreSeekBar;
    private final TextView scoreText;
    private final AnimationProvider animationProvider;

    private int score;
    private final int DEFAULT_SCORE = 3;

    public ScoreContainer(@NonNull View view) {
        scoreWrapper = (ConstraintLayout) view;
        scoreSeekBar = view.findViewById(R.id.scoreSeekBar);
        scoreText = view.findViewById(R.id.scoreText);
        animationProvider = new AnimationProvider(view);

        scoreSeekBar.setProgress(DEFAULT_SCORE);
        score = DEFAULT_SCORE;
        scoreText.setText(String.valueOf(DEFAULT_SCORE));
        scoreSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int scoreValue, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                score = seekBar.getProgress();
                scoreText.setText(String.valueOf(score));
                // TODO: 점수 저장 API 호출
            }
        });
    }

    public int getScore() {
        return score;
    }

    public void setUiVisible() {
        scoreWrapper.setVisibility(View.VISIBLE);
        scoreWrapper.startAnimation(animationProvider.fadeIn);
    }
}
