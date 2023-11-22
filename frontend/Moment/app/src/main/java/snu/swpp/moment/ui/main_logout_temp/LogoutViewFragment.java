package snu.swpp.moment.ui.main_logout_temp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import snu.swpp.moment.EntryActivity;
import snu.swpp.moment.data.factory.AuthenticationRepositoryFactory;
import snu.swpp.moment.databinding.FragmentLogoutviewBinding;


public class LogoutViewFragment extends Fragment {

    private FragmentLogoutviewBinding binding;
    private String currentDate;
    private AuthenticationRepositoryFactory authenticationRepositoryFactory;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        authenticationRepositoryFactory = new AuthenticationRepositoryFactory(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
        ViewGroup container, Bundle savedInstanceState) {
        LogoutViewModel homeViewModel =
            new ViewModelProvider(this).get(LogoutViewModel.class);

        binding = FragmentLogoutviewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final Button logoutButton = binding.logoutButton;
        System.out.println("#DEBUG: go home");
        logoutButton.setOnClickListener(v -> {
            System.out.println("#DEBUG: logout button clicked");
            authenticationRepositoryFactory.getRepository().logout();
            Intent logoutIntent = new Intent(requireActivity(), EntryActivity.class);
            startActivity(logoutIntent);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}