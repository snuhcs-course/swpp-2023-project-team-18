package snu.swpp.moment.ui.main_userinfoview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import snu.swpp.moment.databinding.FragmentUserinfoviewBinding;

public class UserInfoViewFragment extends Fragment {

    private FragmentUserinfoviewBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        UserInfoViewModel userInfoViewModel =
                new ViewModelProvider(this).get(UserInfoViewModel.class);

        binding = FragmentUserinfoviewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.userinfoview;
        userInfoViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}