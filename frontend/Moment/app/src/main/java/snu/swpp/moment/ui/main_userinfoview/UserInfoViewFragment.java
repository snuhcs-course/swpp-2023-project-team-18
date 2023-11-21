package snu.swpp.moment.ui.main_userinfoview;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.databinding.FragmentUserinfoviewBinding;

public class UserInfoViewFragment extends Fragment {

    private FragmentUserinfoviewBinding binding;
    private UserInfoViewModel viewModel;

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

        binding.logoutButton.setActivated(true);
        updateUItoNonEditingMode();

        binding.penIcon.setOnClickListener(observer -> {
            updateUItoEditingMode();
        });

        binding.checkIcon.setOnClickListener(observer -> {
            // TODO
            // update nickname

            updateUItoNonEditingMode();
        });

        binding.logoutButton.setOnClickListener(observer -> {
            viewModel.logout();
        });

        binding.usernameEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String nickname = s.toString();
                try {
                    byte[] nicknameBytes = nickname.getBytes("KSC5601");
                    if (nicknameBytes.length > 20) {
                        updateUItoLongNicknameMode();
                    } else {
                        updateUItoEditingMode();
                    }
                } catch (UnsupportedEncodingException e) {
                    updateUItoEditingMode();
                }
            }
        });

        return root;
    }

    private void updateUItoEditingMode() {
        binding.penIcon.setVisibility(View.GONE);
        binding.checkIcon.setVisibility(View.VISIBLE);
        binding.checkIcon.setEnabled(true);
        binding.checkIcon.setActivated(true);
        binding.usernameEdittext.setInputType(InputType.TYPE_CLASS_TEXT);
        binding.nicknameLengthWarningText.setVisibility(View.GONE);
    }

    private void updateUItoNonEditingMode() {
        binding.checkIcon.setVisibility(View.GONE);
        binding.penIcon.setVisibility(View.VISIBLE);
        binding.usernameEdittext.setText(viewModel.getNickname());
        binding.usernameEdittext.setInputType(InputType.TYPE_NULL);
        binding.usernameEdittext.setGravity(Gravity.CENTER);

        binding.nicknameLengthWarningText.setVisibility(View.GONE);
    }

    private void updateUItoLongNicknameMode() {
        binding.checkIcon.setVisibility(View.GONE);
        binding.nicknameLengthWarningText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}