package snu.swpp.moment.ui.main_userinfoview;

import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import snu.swpp.moment.R;
import snu.swpp.moment.databinding.UserInfoWrapperBinding;

enum UserInfoWrapperState {
    READ, EDIT, EDIT_LIMIT_EXCEEDED,
}

public class UserInfoWrapperContainer {

    private final UserInfoWrapperBinding binding;
    private UserInfoWrapperState state;

    private final int MAX_LENGTH = 20;

    public UserInfoWrapperContainer(UserInfoWrapperBinding binding) {
        this.binding = binding;
        this.state = UserInfoWrapperState.READ;

        // 닉네임 자수 제한 검사
        binding.nicknameEdittext.addTextChangedListener(new TextWatcher() {
            boolean isLimitExceeded = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                int len = s.toString().length();
                if (len > MAX_LENGTH) {
                    isLimitExceeded = true;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int len = s.toString().length();
                if (len > MAX_LENGTH) {
                    setState(UserInfoWrapperState.EDIT_LIMIT_EXCEEDED);
                } else if (isLimitExceeded) {
                    setState(UserInfoWrapperState.EDIT);
                }
            }
        });
    }

    public void setPenIconOnClickListener(View.OnClickListener listener) {
        binding.penIcon.setOnClickListener(listener);
    }

    public UserInfoWrapperState getState() {
        return state;
    }

    public void setState(UserInfoWrapperState state) {
        Log.d("UserInfoWrapperContainer", String.format("setState: %s -> %s", this.state, state));
        this.state = state;

        updatePenIcon();
        updateEditText();
        updateWarningText();
    }

    private void updatePenIcon() {
        switch (state) {
            case READ:
                binding.penIcon.setEnabled(true);
                setPenIconImage(R.drawable.pen);
                break;
            case EDIT:
                binding.penIcon.setEnabled(true);
                setPenIconImage(R.drawable.moment_write_button);
                break;
            case EDIT_LIMIT_EXCEEDED:
                binding.penIcon.setEnabled(false);
                setPenIconImage(R.drawable.moment_write_inactivate);
                break;
        }
    }

    private void setPenIconImage(@DrawableRes int resId) {
        binding.penIcon.setImageResource(resId);
        binding.penIcon.setTag(resId);  // for UI testing
    }

    private void updateEditText() {
        switch (state) {
            case READ:
                binding.nicknameEdittext.setEnabled(false);
                break;
            case EDIT:
            case EDIT_LIMIT_EXCEEDED:
                binding.nicknameEdittext.setEnabled(true);
                break;
        }
    }

    private void updateWarningText() {
        switch (state) {
            case READ:
            case EDIT:
                binding.nicknameLengthWarningText.setVisibility(View.INVISIBLE);
                break;
            case EDIT_LIMIT_EXCEEDED:
                binding.nicknameLengthWarningText.setVisibility(View.VISIBLE);
                break;
        }
    }

    public String getNickname() {
        return binding.nicknameEdittext.getText().toString();
    }

    public void setNickname(String nickname) {
        binding.nicknameEdittext.setText(nickname);
    }

    public void setCreatedAtText(int dayCount) {
        int digit = Integer.toString(dayCount).length();
        String text = "오늘까지 " + dayCount + "일째\n하루를 남기고 있어요";
        binding.createdAtText.setText(text);

        Spannable span = (Spannable) binding.createdAtText.getText();
        span.setSpan(
            new ForegroundColorSpan(
                ContextCompat.getColor(binding.getRoot().getContext(), R.color.red)),
            5,
            5 + digit,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }
}
