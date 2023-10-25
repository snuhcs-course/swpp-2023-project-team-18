package snu.swpp.moment.ui.main_writeview.DaySlide;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import snu.swpp.moment.R;

public class TagBoxContainer {

    private final ConstraintLayout tagWrapper;
    private final EditText tagEditText;
    private final TextView limitHelpText;
    private final MutableLiveData<Boolean> isLimitExceeded = new MutableLiveData<>(false);

    private final int MAX_TAGS = 10;

    public TagBoxContainer(@NonNull View view) {
        tagWrapper = (ConstraintLayout) view;
        tagEditText = view.findViewById(R.id.tagsEditText);
        limitHelpText = view.findViewById(R.id.tagLimitHelpText);

        tagEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                List<String> currentTags = parseTags(s.toString());
                if (currentTags.size() > MAX_TAGS) {
                    // 개수 제한 초과
                    isLimitExceeded.setValue(true);
                    limitHelpText.setVisibility(View.VISIBLE);
                    tagEditText.setTextColor(
                        ContextCompat.getColor(tagEditText.getContext(), R.color.red));
                } else {
                    isLimitExceeded.setValue(false);
                    limitHelpText.setVisibility(View.GONE);
                    tagEditText.setTextColor(
                        ContextCompat.getColor(tagEditText.getContext(), R.color.black));
                }
            }
        });
    }

    public List<String> getTags() {
        return parseTags(tagEditText.getText().toString());
    }

    public boolean isLimitExceeded() {
        Boolean value = isLimitExceeded.getValue();
        return Objects.requireNonNullElse(value, false);
    }

    public void setVisibility(int visibility) {
        tagWrapper.setVisibility(visibility);
    }

    public void freeze() {
        tagEditText.setEnabled(false);
        tagEditText.setHint("");
    }

    public void setLimitObserver(Observer<Boolean> observer) {
        isLimitExceeded.observeForever(observer);
    }

    private static List<String> parseTags(String s) {
        List<String> hashtags = new ArrayList<>();

        Pattern pattern = Pattern.compile("#\\w+");
        Matcher matcher = pattern.matcher(s);

        while (matcher.find()) {
            // Add the matched hashtag (without the '#' symbol) to the list
            String hashtag = matcher.group().substring(1);
            hashtags.add(hashtag);
        }

        return hashtags;
    }
}
