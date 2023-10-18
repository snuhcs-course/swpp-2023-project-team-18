package snu.swpp.moment.ui.main_logout_temp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import snu.swpp.moment.EntryActivity;
import snu.swpp.moment.data.AuthenticationRepository;
import snu.swpp.moment.databinding.FragmentLogoutviewBinding;


public class LogoutViewFragment extends Fragment {

    private FragmentLogoutviewBinding binding;
    private String currentDate;
    private AuthenticationRepository authenticationRepository;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LogoutViewModel homeViewModel =
                new ViewModelProvider(this).get(LogoutViewModel.class);

        binding = FragmentLogoutviewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final Button logoutButton = binding.logoutButton;
        try {
            authenticationRepository = AuthenticationRepository.getInstance(getContext());
            System.out.println("#DEBUG: go home");
            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("#DEBUG: logout button clicked");
                    authenticationRepository.logout();
                    Intent logoutIntent= new Intent(getActivity(), EntryActivity.class);
                    startActivity(logoutIntent);
                }
            });
        } catch (GeneralSecurityException e) {
            System.out.println("#DEBUG: General");
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("#DEBUG: IO");
            throw new RuntimeException(e);
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}