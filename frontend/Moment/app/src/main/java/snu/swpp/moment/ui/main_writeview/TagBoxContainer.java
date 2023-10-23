package snu.swpp.moment.ui.main_writeview;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.kizitonwose.calendar.view.ViewContainer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import snu.swpp.moment.R;

public class TagBoxContainer extends ViewContainer {

    private final EditText tagBox;
    private final TextView limitHelpText;
    private boolean isLimitExceeded = false;

    private final int MAX_TAGS = 10;

    public TagBoxContainer(View view) {
        super(view);

        tagBox = view.findViewById(R.id.tagsEditText);
        limitHelpText = view.findViewById(R.id.tagLimitHelpText);

        tagBox.addTextChangedListener(new TextWatcher() {
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
                    isLimitExceeded = true;
                    limitHelpText.setVisibility(View.VISIBLE);
                    tagBox.setTextColor(
                        ContextCompat.getColor(tagBox.getContext(), R.color.darkgray));
                } else {
                    isLimitExceeded = false;
                    limitHelpText.setVisibility(View.GONE);
                    tagBox.setTextColor(ContextCompat.getColor(tagBox.getContext(), R.color.black));
                }
            }
        });
    }

    public boolean isLimitExceeded() {
        return isLimitExceeded;
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
