package snu.swpp.moment.ui.main_writeview;

import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;
import androidx.core.content.res.ResourcesCompat;
import java.util.Arrays;
import java.util.List;
import snu.swpp.moment.R;

public class EmotionGridContainer {

    private final List<TextView> textButtonList;
    private int selectedEmotion = -1;
    private boolean isFrozen = false;


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

        Typeface maruburiLight = ResourcesCompat.getFont(view.getContext(),
            R.font.maruburi_light);
        Typeface maruburiBold = ResourcesCompat.getFont(view.getContext(),
            R.font.maruburi_bold);

        for (int i = 0; i < textButtonList.size(); i++) {
            TextView textButton = textButtonList.get(i);
            final int _i = i;
            textButton.setOnClickListener(v -> {
                if (isFrozen) {
                    return;
                }

                int previousSelectedEmotion = selectedEmotion;
                if (previousSelectedEmotion > -1) {
                    textButtonList.get(previousSelectedEmotion)
                        .setTypeface(maruburiLight, R.color.black);
                }

                selectedEmotion = _i;
                textButton.setTypeface(maruburiBold, R.color.red);
            });
        }
    }

    public int getSelectedEmotion() {
        return selectedEmotion;
    }

    public void setFrozen(boolean frozen) {
        isFrozen = frozen;
    }
}
