package snu.swpp.moment.ui.main_statview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import java.text.SimpleDateFormat;
import java.util.Locale;
import snu.swpp.moment.databinding.FragmentStatviewBinding;

public class StatViewFragment extends Fragment {

    private FragmentStatviewBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
        ViewGroup container, Bundle savedInstanceState) {
        StatViewModel statViewModel =
            new ViewModelProvider(this).get(StatViewModel.class);

        binding = FragmentStatviewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        final TextView textView = binding.statview;
        statViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}