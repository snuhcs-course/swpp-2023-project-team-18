package snu.swpp.moment.ui.main_userinfoview;

import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import snu.swpp.moment.R;
import snu.swpp.moment.databinding.UserInfoWrapperBinding;

public class UserInfoWrapperContainer {

    private ImageButton penIcon;
    private EditText nicknameEditText;
    private TextView nicknameLengthWarningText;

    public UserInfoWrapperContainer(UserInfoWrapperBinding binding) {
        penIcon = binding.penIcon;
        nicknameEditText = binding.nicknameEdittext;
        nicknameLengthWarningText = binding.nicknameLengthWarningText;
    }

    public void updateUI(int state) {
        if (state == FragmentState.READ) {
            updateUItoReadingMode();
        } else if (state == FragmentState.EDIT) {
            updateUItoEditingMode();
        } else if (state == FragmentState.EDIT_ERROR) {
            updateUItoLongNicknameMode();
        }
    }

    private void updateUItoEditingMode() {
        Log.d("UserInfoWrapperContainer", "editing");
        penIcon.setEnabled(true);
        penIcon.setImageResource(R.drawable.moment_write_button);
        nicknameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        nicknameLengthWarningText.setVisibility(View.GONE);
    }

    private void updateUItoReadingMode() {
        Log.d("UserInfoWrapperContainer", "non editing");
        penIcon.setEnabled(true);
        penIcon.setImageResource(R.drawable.pen);
        nicknameEditText.setInputType(InputType.TYPE_NULL);
        nicknameEditText.setGravity(Gravity.CENTER);

        nicknameLengthWarningText.setVisibility(View.GONE);
    }

    private void updateUItoLongNicknameMode() {
        Log.d("UserInfoWrapperContainer", "long nickname");
        penIcon.setEnabled(false);
        penIcon.setImageResource(R.drawable.moment_write_inactivate);
        nicknameLengthWarningText.setVisibility(View.VISIBLE);
    }

}
