package snu.swpp.moment.ui.main_writeview.component;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import snu.swpp.moment.R;
import snu.swpp.moment.utils.AnimationProvider;


enum TagBoxContainerState {
    INVISIBLE,
    WRITING,
    COMPLETE,
}


public class TagBoxContainer {

    private TagBoxContainerState state;

    private final ConstraintLayout tagWrapper;
    private final EditText tagEditText;
    private final TextView tagHelpText;
    private final TextView limitHelpText;
    private final AnimationProvider animationProvider;
    private boolean isLengthLimitExceeded = false;
    private final MutableLiveData<Boolean> isLimitExceeded = new MutableLiveData<>(false);

    private int characterCount = 0;
    private final int MAX_TAGS = 10;

    public TagBoxContainer(@NonNull View view) {
        tagWrapper = (ConstraintLayout) view;
        tagEditText = view.findViewById(R.id.tagsEditText);
        tagHelpText = view.findViewById(R.id.tagHelpText);
        limitHelpText = view.findViewById(R.id.tagLimitHelpText);
        animationProvider = new AnimationProvider(view);

        tagEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("TagBoxContainer", "afterTextChanged: " + s.toString());
                List<String> currentTags = parseTags(s.toString());
                if (currentTags.size() > MAX_TAGS) {
                    // 개수 제한 초과
                    isLimitExceeded.setValue(true);
                    isLengthLimitExceeded = true;
                    limitHelpText.setVisibility(View.VISIBLE);
                    tagEditText.setTextColor(
                        ContextCompat.getColor(tagEditText.getContext(), R.color.red));
                } else if (isLengthLimitExceeded) {
                    isLengthLimitExceeded = false;
                    isLimitExceeded.setValue(false);
                    limitHelpText.setVisibility(View.GONE);
                    tagEditText.setTextColor(
                        ContextCompat.getColor(tagEditText.getContext(), R.color.black));
                }

                if (s.toString().length() > characterCount && s.toString().endsWith(" ")) {
                    s.append("#");
                }
                characterCount = s.toString().length();
            }
        });
    }

    public void setState(TagBoxContainerState state) {
        Log.d("TagBoxContainer", String.format("state: %s -> %s", this.state, state));
        this.state = state;

        updateTagWrapper();
        updateTagEditText();
    }

    private void updateTagWrapper() {
        switch (state) {
            case INVISIBLE:
                tagWrapper.setVisibility(View.GONE);
                tagHelpText.setText(R.string.tag_help_text);
                break;
            case WRITING:
                tagWrapper.setVisibility(View.VISIBLE);
                tagWrapper.startAnimation(animationProvider.fadeIn);
                tagHelpText.setText(R.string.tag_help_text);
                break;
            case COMPLETE:
                tagWrapper.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updateTagEditText() {
        switch (state) {
            case INVISIBLE:
                tagEditText.setText("");
                freeze(false);
                break;
            case WRITING:
                freeze(false);
                break;
            case COMPLETE:
                freeze(true);
                break;
        }
    }

    public String getTags() {
        return tagEditText.getText().toString();
    }

    public void setTags(@NonNull List<String> tags) {
        StringBuilder sb = new StringBuilder();
        for (String tag : tags) {
            sb.append("#").append(tag).append(" ");
        }
        tagEditText.setText(sb.toString());
    }

//    public void setUiVisible() {
//        tagWrapper.setVisibility(View.VISIBLE);
//        tagWrapper.startAnimation(animationProvider.fadeIn);
//    }

//    public void resetUi() {
//        freeze(false);
//        tagEditText.setText("");
//        tagHelpText.setText(R.string.tag_help_text);
//        tagWrapper.setVisibility(View.GONE);
//    }

    public void setHelpText(String text) {
        tagHelpText.setText(text);
    }

    // FIXME: will be changed to private
    public void freeze(boolean freeze) {
        tagEditText.setEnabled(!freeze);
        if (freeze) {
            tagEditText.setHint("");
        } else {
            tagEditText.setHint(R.string.tag_hint);
        }
    }

    public void observeLimit(LifecycleOwner lifecycleOwner, Observer<Boolean> observer) {
        isLimitExceeded.observe(lifecycleOwner, observer);
    }

    public void removeObservers(LifecycleOwner lifecycleOwner) {
        isLimitExceeded.removeObservers(lifecycleOwner);
    }

    private static List<String> parseTags(String s) {
        List<String> hashtags = new ArrayList<>();

        Pattern pattern = Pattern.compile("#\\w+");
        Matcher matcher = pattern.matcher(s);

        while (matcher.find()) {
            // '#' 제거 후 태그 저장
            String hashtag = matcher.group().substring(1);
            hashtags.add(hashtag);
        }

        return hashtags;
    }
}
