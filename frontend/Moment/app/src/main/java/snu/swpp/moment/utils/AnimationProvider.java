package snu.swpp.moment.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import snu.swpp.moment.R;

public class AnimationProvider {

    public final Animation fadeIn;
    public final Animation delayedFadeIn;
    public final Animation fadeOut;
    public final Animation longFadeOut;
    public final Animation fadeInOut;

    public AnimationProvider(View view) {
        fadeIn = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in);
        delayedFadeIn = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in);
        delayedFadeIn.setStartOffset(500);
        fadeOut = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out);
        longFadeOut = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out);
        longFadeOut.setDuration(500);
        fadeInOut = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in_out);
    }

    public AnimationProvider(Context context) {
        fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        delayedFadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        delayedFadeIn.setStartOffset(500);
        fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out);
        longFadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out);
        longFadeOut.setDuration(500);
        fadeInOut = AnimationUtils.loadAnimation(context, R.anim.fade_in_out);
    }
}
