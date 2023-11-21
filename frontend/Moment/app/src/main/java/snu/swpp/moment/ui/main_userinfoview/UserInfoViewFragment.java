package snu.swpp.moment.ui.main_userinfoview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import java.io.IOException;
import java.security.GeneralSecurityException;
import snu.swpp.moment.EntryActivity;
import snu.swpp.moment.R;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.databinding.FragmentUserinfoviewBinding;

public class UserInfoViewFragment extends Fragment {

    private FragmentUserinfoviewBinding binding;
    private UserInfoWrapperContainer userInfoWrapperContainer;
    private UserInfoViewModel viewModel;
    private int fragmentState = UserInfoViewFragmentState.READ;
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

        userInfoWrapperContainer = new UserInfoWrapperContainer(binding.userInfoWrapper);

        binding.userInfoWrapper.nicknameEdittext.setText("닉네임"); // TODO

        binding.logoutButton.setActivated(true);

        updateUI();

        binding.userInfoWrapper.penIcon.setOnClickListener(observer -> {
            if (fragmentState == UserInfoViewFragmentState.EDIT) {
                updateFragmentState(UserInfoViewFragmentState.READ);
            } else {
                updateFragmentState(UserInfoViewFragmentState.EDIT);
            }
        });

        int num = 21; // TODO
        setCreatedAtText(binding, num);

        binding.logoutButton.setOnClickListener(observer -> {
            viewModel.logout();
            startActivity(new Intent(requireActivity(), EntryActivity.class));
        });

        binding.userInfoWrapper.nicknameEdittext.addTextChangedListener(new TextWatcher() {
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
                    updateFragmentState(UserInfoViewFragmentState.EDIT_ERROR);
                } else if (isLongNicknameMode) {
                    updateFragmentState(UserInfoViewFragmentState.EDIT);
                }
            }
        });

        return root;
    }

    private void updateFragmentState(int state) {
        if (fragmentState != state) {
            this.fragmentState = state;
            updateUI();
        }
    }

    private void updateUI() {
        userInfoWrapperContainer.updateUI(fragmentState);
    }

    private void setCreatedAtText(FragmentUserinfoviewBinding binding, int num) {
        int digit = Integer.toString(num).length();
        String text = "오늘까지 " + num + "일째\n 하루를 남기고 있어요";
        binding.userInfoWrapper.createdAtText.setText(text);

        Spannable span = (Spannable) binding.userInfoWrapper.createdAtText.getText();
        span.setSpan(
            new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)),
            5,
            5 + digit,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}