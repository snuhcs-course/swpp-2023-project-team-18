package snu.swpp.moment.ui.main_writeview.component;

import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    private final List<LinearLayout> linearLayoutList;
    private final List<ImageView> imageViewList;

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

        linearLayoutList = Arrays.asList(
            view.findViewById(R.id.emotionSun1),
            view.findViewById(R.id.emotionSun2),
            view.findViewById(R.id.emotionSunCloud1),
            view.findViewById(R.id.emotionSunCloud2),
            view.findViewById(R.id.emotionCloud1),
            view.findViewById(R.id.emotionCloud2),
            view.findViewById(R.id.emotionRain1),
            view.findViewById(R.id.emotionRain2),
            view.findViewById(R.id.emotionLightning1),
            view.findViewById(R.id.emotionLightning2)
        );

        imageViewList = Arrays.asList(
            view.findViewById(R.id.emotionSunRow1),
            view.findViewById(R.id.emotionSunRow2),
            view.findViewById(R.id.emotionSunCloudRow1),
            view.findViewById(R.id.emotionSunCloudRow2),
            view.findViewById(R.id.emotionCloudRow1),
            view.findViewById(R.id.emotionCloudRow2),
            view.findViewById(R.id.emotionRainRow1),
            view.findViewById(R.id.emotionRainRow2),
            view.findViewById(R.id.emotionLightningRow1),
            view.findViewById(R.id.emotionLightningRow2)
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
            textButton.setOnClickListener(v -> selectEmotion(emotionIdx));
        }

        for (int i = 0; i < linearLayoutList.size(); i++) {
            LinearLayout linearLayout = linearLayoutList.get(i);
            final int linearLayoutIndex = i;
            linearLayout.setOnClickListener(v -> selectEmotion(linearLayoutIndex));
        }

        for (int i = 0; i < imageViewList.size(); i++) {
            ImageView imageView = imageViewList.get(i);
            final int imageViewIndex = i;
            imageView.setOnClickListener(v -> selectEmotion(imageViewIndex));
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
            case SELECTING:
                selectEmotion(EmotionMap.INVALID_EMOTION);
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

            // 기존 감정의 ImageView 색상을 원래대로 변경
            ImageView previousImage = imageViewList.get(previous);
            previousImage.clearColorFilter();

            linearLayoutList.get(previous).setSelected(false);
        }
        if (0 <= emotion && emotion < textButtonList.size()) {
            // 새로운 감정 선택
            TextView newButton = textButtonList.get(emotion);
            newButton.setTypeface(maruburiBold);
            newButton.setTextColor(colorRed);

            // 새로운 감정의 ImageView 색상을 빨간색으로 변경
            ImageView newImage = imageViewList.get(emotion);
            newImage.setColorFilter(colorRed, android.graphics.PorterDuff.Mode.SRC_IN);

            linearLayoutList.get(emotion).setSelected(true);
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

    private void freeze(boolean freeze) {
        for (int i = 0; i < textButtonList.size(); i++) {
            TextView textButton = textButtonList.get(i);
            textButton.setClickable(!freeze);

            //Linear layout
            LinearLayout linearLayout = linearLayoutList.get(i);
            linearLayout.setClickable(!freeze);

            // ImageView Layout
            ImageView imageView = imageViewList.get(i);
            imageView.setClickable(!freeze);
        }
    }
}
