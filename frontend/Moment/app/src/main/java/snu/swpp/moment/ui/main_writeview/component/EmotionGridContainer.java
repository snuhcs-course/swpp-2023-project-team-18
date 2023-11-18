package snu.swpp.moment.ui.main_writeview.component;

import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import snu.swpp.moment.R;
import snu.swpp.moment.utils.EmotionMap;

public class EmotionGridContainer {

    private final List<TextView> textButtonList;
    private final List<ImageView> imageViewList;
    private final MutableLiveData<Integer> selectedEmotion = new MutableLiveData<>(-1);

    private final Typeface maruburiLight;
    private final Typeface maruburiBold;
    private final int colorBlack;
    private final int colorRed;


    public EmotionGridContainer(View view) {
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
        imageViewList=Arrays.asList(
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

        for (int i = 0; i < textButtonList.size(); i++) {
            TextView textButton = textButtonList.get(i);
            ImageView imageView = imageViewList.get(i);
            final int _i = i;
            textButton.setOnClickListener(v -> {
                int previousSelectedEmotion = selectedEmotion.getValue();
                if (0 <= previousSelectedEmotion
                    && previousSelectedEmotion < textButtonList.size()) {
                    TextView previousButton = textButtonList.get(previousSelectedEmotion);
                    previousButton.setTypeface(maruburiLight);
                    previousButton.setTextColor(colorBlack);
                }

                selectedEmotion.setValue(_i);
                textButton.setTypeface(maruburiBold);
                textButton.setTextColor(colorRed);

                Log.d("EmotionGridContainer",
                    String.format("emotion: %d -> %d", previousSelectedEmotion,
                        selectedEmotion.getValue()));
            });
            imageView.setOnClickListener(v -> {
                int previousSelectedEmotion = selectedEmotion.getValue();
                if (0 <= previousSelectedEmotion
                    && previousSelectedEmotion < textButtonList.size()) {
                    TextView previousButton = textButtonList.get(previousSelectedEmotion);
                    previousButton.setTypeface(maruburiLight);
                    previousButton.setTextColor(colorBlack);
                }

                selectedEmotion.setValue(_i);
                textButton.setTypeface(maruburiBold);
                textButton.setTextColor(colorRed);

                Log.d("EmotionGridContainer",
                    String.format("emotion: %d -> %d", previousSelectedEmotion,
                        selectedEmotion.getValue()));
            });

        }
    }

    public int getSelectedEmotion() {
        Integer value = selectedEmotion.getValue();
        return Objects.requireNonNullElse(value, -1);
    }

    public void selectEmotion(int emotion) {
        if (emotion == EmotionMap.INVALID_EMOTION) {
            return;
        }
        TextView textButton = textButtonList.get(emotion);
        textButton.performClick();
    }

    public void observeSelectedEmotion(Observer<Integer> observer) {
        selectedEmotion.observeForever(observer);
    }

    public void resetUi() {
        freeze(false);
        selectedEmotion.setValue(EmotionMap.INVALID_EMOTION);
        for (TextView textButton : textButtonList) {
            textButton.setTypeface(maruburiLight);
            textButton.setTextColor(colorBlack);
        }
    }

    public void freeze(boolean freeze) {
        for (int i = 0; i < textButtonList.size(); i++) {
            TextView textButton = textButtonList.get(i);
            textButton.setClickable(!freeze);
        }
    }
}
