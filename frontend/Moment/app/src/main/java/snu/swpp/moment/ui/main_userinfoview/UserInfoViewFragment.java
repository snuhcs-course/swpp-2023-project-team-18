package snu.swpp.moment.ui.main_userinfoview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import snu.swpp.moment.EntryActivity;
import snu.swpp.moment.R;
import snu.swpp.moment.data.factory.AuthenticationRepositoryFactory;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.databinding.FragmentUserinfoviewBinding;

public class UserInfoViewFragment extends Fragment {

    private FragmentUserinfoviewBinding binding;
    private UserInfoWrapperContainer userInfoWrapperContainer;
    private UserInfoViewModel viewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        AuthenticationRepository authenticationRepository =
            new AuthenticationRepositoryFactory(context).getRepository();

        viewModel = new UserInfoViewModelFactory(authenticationRepository)
            .create(UserInfoViewModel.class);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
        ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentUserinfoviewBinding.inflate(inflater, container, false);

        userInfoWrapperContainer = new UserInfoWrapperContainer(binding.userInfoWrapper);
        userInfoWrapperContainer.setState(UserInfoWrapperState.READ);
        userInfoWrapperContainer.setNickname("닉네임"); // TODO

        binding.logoutButton.setActivated(true);

        int num = 21; // TODO
        userInfoWrapperContainer.setCreatedAtText(num);

        // 로그아웃 버튼
        binding.logoutButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(binding.getRoot().getContext(),
                R.style.DialogTheme);
            builder.setMessage("로그아웃 하실래요?");

            builder.setPositiveButton(R.string.popup_yes, (dialog, id) -> {
                    viewModel.logout();
                    startActivity(new Intent(requireActivity(), EntryActivity.class));
                })
                .setNegativeButton(R.string.popup_no, (dialog, id) -> {
                });
            builder.create().show();
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}