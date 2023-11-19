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
            // #의 연속 입력을 막기 위한 변수 (스페이스바만 계속 누르는 경우 스페이스바 무시)
            private boolean isHashJustEntered = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 사용자가 삭제를 눌렀는지 확인 (스페이스바 무시 로직에서 예외 처리)
                if (count > 0 && after == 0) {
                    isHashJustEntered = false;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 사용자가 스페이스바를 눌렀는지 확인
                if (count > 0 && s.subSequence(start, start + count).toString().equals(" ")) {
                    if (s.length() > 1 && s.charAt(s.length() - 2) == '#') {
                        // # 뒤에 스페이스바가 눌렸다면
                        isHashJustEntered = true;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isHashJustEntered) {
                    // # 뒤에 스페이스바가 눌렸을 경우, 스페이스바 제거
                    s.delete(s.length() - 1, s.length());
                    isHashJustEntered = false;
                } else if (s.length() > 0 && s.charAt(0) != '#') {
                    // 사용자 입력이 시작되면 맨 앞에 # 추가
                    s.insert(0, "#");
                }
                Log.d("TagBoxContainer", "afterTextChanged: " + s);
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

    public void setHelpText(String text) {
        tagHelpText.setText(text);
    }

    public void observeLimit(LifecycleOwner lifecycleOwner, Observer<Boolean> observer) {
        isLimitExceeded.observe(lifecycleOwner, observer);
    }

    public void removeObservers(LifecycleOwner lifecycleOwner) {
        isLimitExceeded.removeObservers(lifecycleOwner);
    }

    private void freeze(boolean freeze) {
        tagEditText.setEnabled(!freeze);
        if (freeze) {
            tagEditText.setHint("");
        } else {
            tagEditText.setHint(R.string.tag_hint);
        }
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
