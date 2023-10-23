package snu.swpp.moment.ui.main_writeview;

import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;
import androidx.core.content.res.ResourcesCompat;
import com.kizitonwose.calendar.view.ViewContainer;
import java.util.Arrays;
import java.util.List;
import snu.swpp.moment.R;

public class EmotionGridContainer extends ViewContainer {

    private final List<TextView> textButtonList = Arrays.asList(
        getView().findViewById(R.id.excited1Button),
        getView().findViewById(R.id.excited2Button),
        getView().findViewById(R.id.happy1Button),
        getView().findViewById(R.id.happy2Button),
        getView().findViewById(R.id.normal1Button),
        getView().findViewById(R.id.normal2Button),
        getView().findViewById(R.id.sad1Button),
        getView().findViewById(R.id.sad2Button),
        getView().findViewById(R.id.angry1Button),
        getView().findViewById(R.id.angry2Button)
    );
    private int selectedEmotion = -1;
    private boolean isFrozen = false;


    public EmotionGridContainer(View view) {
        super(view);

        Typeface maruburiLight = ResourcesCompat.getFont(getView().getContext(),
            R.font.maruburi_light);
        Typeface maruburiBold = ResourcesCompat.getFont(getView().getContext(),
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
