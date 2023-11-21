package snu.swpp.moment.ui.main_userinfoview;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import java.io.IOException;
import java.security.GeneralSecurityException;
import snu.swpp.moment.R;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.databinding.FragmentUserinfoviewBinding;

public class UserInfoViewFragment extends Fragment {

    private FragmentUserinfoviewBinding binding;
    private UserInfoViewModel viewModel;
    private final int MAX_BYTE = 40;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        AuthenticationRepository authenticationRepository = null;
        try {
            authenticationRepository = AuthenticationRepository.getInstance(context);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        viewModel = new UserInfoViewModelFactory(authenticationRepository)
            .create(UserInfoViewModel.class);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
        ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentUserinfoviewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.nicknameEdittext.setText("닉네임"); // TODO
        updateUItoNonEditingMode();
        binding.logoutButton.setActivated(true);

        binding.penIcon.setOnClickListener(observer -> {
            updateUItoEditingMode();
        });

        binding.checkIcon.setOnClickListener(observer -> {
            // TODO
            // update nickname

            updateUItoNonEditingMode();
        });

        int digit = 2; // TODO
        setCreatedAtText(binding, digit);

        binding.logoutButton.setOnClickListener(observer -> {
            viewModel.logout();
        });

        binding.nicknameEdittext.addTextChangedListener(new TextWatcher() {
            boolean isLongNicknameMode = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                int bytes = s.toString().getBytes().length;
                if (bytes > MAX_BYTE) {
                    isLongNicknameMode = true;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int nicknameBytes = s.toString().getBytes().length;
                if (nicknameBytes > MAX_BYTE) {
                    updateUItoLongNicknameMode();
                } else if (isLongNicknameMode) {
                    updateUItoEditingMode();
                }
            }
        });

        return root;
    }

    private void setCreatedAtText(FragmentUserinfoviewBinding binding, int digit) {
        Spannable span = (Spannable) binding.createdAtText.getText();
        span.setSpan(
            new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)),
            5,
            5 + digit,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    private void updateUItoEditingMode() {
        Log.d("UserInfoViewFragment", "editing");
        binding.penIcon.setVisibility(View.GONE);
        binding.checkIcon.setVisibility(View.VISIBLE);
        binding.checkIcon.setEnabled(true);
        binding.checkIcon.setActivated(true);
        binding.nicknameEdittext.setInputType(InputType.TYPE_CLASS_TEXT);
        binding.nicknameLengthWarningText.setVisibility(View.GONE);
    }

    private void updateUItoNonEditingMode() {
        Log.d("UserInfoViewFragment", "non editing");
        binding.checkIcon.setVisibility(View.GONE);
        binding.penIcon.setVisibility(View.VISIBLE);
        //binding.usernameEdittext.setText(viewModel.getNickname());
        binding.nicknameEdittext.setInputType(InputType.TYPE_NULL);
        binding.nicknameEdittext.setGravity(Gravity.CENTER);

        binding.nicknameLengthWarningText.setVisibility(View.GONE);
    }

    private void updateUItoLongNicknameMode() {
        Log.d("UserInfoViewFragment", "long nickname");
        binding.checkIcon.setVisibility(View.GONE);
        binding.nicknameLengthWarningText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}