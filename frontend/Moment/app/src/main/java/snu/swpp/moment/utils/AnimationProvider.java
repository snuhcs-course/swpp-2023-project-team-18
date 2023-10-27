package snu.swpp.moment.utils;

import android.view.View;
import android.view.animation.Animation;
import snu.swpp.moment.R;

public class AnimationProvider {

    public final Animation fadeIn;
    public final Animation delayedFadeIn;
    public final Animation fadeOut;

    public AnimationProvider(View view) {
        fadeIn = android.view.animation.AnimationUtils.loadAnimation(view.getContext(),
            R.anim.fade_in);
        delayedFadeIn = android.view.animation.AnimationUtils.loadAnimation(view.getContext(),
            R.anim.fade_in);
        delayedFadeIn.setStartOffset(500);
        fadeOut = android.view.animation.AnimationUtils.loadAnimation(view.getContext(),
            R.anim.fade_out);
    }
}
