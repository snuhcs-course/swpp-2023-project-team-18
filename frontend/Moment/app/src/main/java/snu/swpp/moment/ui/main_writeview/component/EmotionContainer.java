package snu.swpp.moment.ui.main_writeview.component;

import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import snu.swpp.moment.R;
import snu.swpp.moment.utils.AnimationProvider;
import snu.swpp.moment.utils.EmotionMap;

enum EmotionContainerState {
    INVISIBLE,
    SELECTING,
    COMPLETE,
}

public class EmotionContainer {

    private EmotionContainerState state;

    private final ConstraintLayout emotionWrapper;
    private final TextView emotionHelpText;
    private final List<TextView> textButtonList;
    private final MutableLiveData<Integer> selectedEmotion = new MutableLiveData<>(-1);

    private final Typeface maruburiLight;
    private final Typeface maruburiBold;
    private final int colorBlack;
    private final int colorRed;
    private final AnimationProvider animationProvider;


    public EmotionContainer(View view) {
        emotionWrapper = (ConstraintLayout) view;
        emotionHelpText = view.findViewById(R.id.emotion_help_text);
        textButtonList = Arrays.asList(
            view.findViewById(R.id.excited1Button),
            view.findViewById(R.id.excited2Button),
            view.findViewById(R.id.happy1Button),
            view.findViewById(R.id.happy2Button),
            view.findViewById(R.id.normal1Button),
            view.findViewById(R.id.normal2Button),
            view.findViewById(R.id.sad1Button),
            view.findViewById(R.id.sad2Button),
            view.findViewById(R.id.angry1Button),
            view.findViewById(R.id.angry2Button)
        );

        maruburiLight = ResourcesCompat.getFont(view.getContext(),
            R.font.maruburi_regular);
        maruburiBold = ResourcesCompat.getFont(view.getContext(),
            R.font.maruburi_bold);
        colorBlack = ContextCompat.getColor(view.getContext(), R.color.black);
        colorRed = ContextCompat.getColor(view.getContext(), R.color.red);
        animationProvider = new AnimationProvider(view);

        for (int i = 0; i < textButtonList.size(); i++) {
            TextView textButton = textButtonList.get(i);
            final int emotionIdx = i;
            textButton.setOnClickListener(v -> {
                selectEmotion(emotionIdx);
            });
        }
    }

    public void setState(EmotionContainerState state) {
        Log.d("EmotionContainer", String.format("state: %s -> %s", this.state, state));
        this.state = state;

        updateEmotionWrapper();
        updateEmotionGrid();
    }

    private void updateEmotionWrapper() {
        switch (state) {
            case INVISIBLE:
                emotionWrapper.setVisibility(View.GONE);
                emotionHelpText.setText(R.string.emotion_help_text);
                break;
            case SELECTING:
                emotionWrapper.setVisibility(View.VISIBLE);
                emotionWrapper.startAnimation(animationProvider.fadeIn);
                emotionHelpText.setText(R.string.emotion_help_text);
                break;
            case COMPLETE:
                emotionWrapper.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void updateEmotionGrid() {
        switch (state) {
            case INVISIBLE:
                selectEmotion(EmotionMap.INVALID_EMOTION);
                freeze(false);
                break;
            case SELECTING:
                freeze(false);
                break;
            case COMPLETE:
                freeze(true);
                break;
        }
    }

    public int getSelectedEmotion() {
        Integer value = selectedEmotion.getValue();
        return Objects.requireNonNullElse(value, -1);
    }

    public void selectEmotion(int emotion) {
        int previous = selectedEmotion.getValue();
        Log.d("EmotionContainer", String.format("emotion: %d -> %d", previous, emotion));

        if (0 <= previous && previous < textButtonList.size()) {
            // 기존 감정 선택 해제
            TextView previousButton = textButtonList.get(previous);
            previousButton.setTypeface(maruburiLight);
            previousButton.setTextColor(colorBlack);
        }
        if (0 <= emotion && emotion < textButtonList.size()) {
            // 새로운 감정 선택
            TextView newButton = textButtonList.get(emotion);
            newButton.setTypeface(maruburiBold);
            newButton.setTextColor(colorRed);
        }
        selectedEmotion.setValue(emotion);
    }

    public void setHelpText(String text) {
        emotionHelpText.setText(text);
    }

    public void observeSelectedEmotion(Observer<Integer> observer) {
        selectedEmotion.observeForever(observer);
    }

    public void removeObservers(LifecycleOwner lifecycleOwner) {
        selectedEmotion.removeObservers(lifecycleOwner);
    }

//    public void setUiVisible() {
//        emotionWrapper.setVisibility(View.VISIBLE);
//    }

//    public void setUiSelectingEmotion() {
//        emotionWrapper.setVisibility(View.VISIBLE);
//        emotionWrapper.startAnimation(animationProvider.fadeIn);
//    }

//    public void resetUi() {
//        freeze(false);
//        emotionHelpText.setText(R.string.emotion_help_text);
//        emotionWrapper.setVisibility(View.GONE);
//        selectedEmotion.setValue(EmotionMap.INVALID_EMOTION);
//        for (TextView textButton : textButtonList) {
//            textButton.setTypeface(maruburiLight);
//            textButton.setTextColor(colorBlack);
//        }
//    }

    // FIXME: will be changed to private
    public void freeze(boolean freeze) {
        for (int i = 0; i < textButtonList.size(); i++) {
            TextView textButton = textButtonList.get(i);
            textButton.setClickable(!freeze);
        }
    }
}
