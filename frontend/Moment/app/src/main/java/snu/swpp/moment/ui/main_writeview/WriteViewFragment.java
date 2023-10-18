package snu.swpp.moment.ui.main_writeview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import snu.swpp.moment.databinding.FragmentWriteviewBinding;


public class WriteViewFragment extends Fragment {

    private FragmentWriteviewBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        WriteViewModel homeViewModel =
                new ViewModelProvider(this).get(WriteViewModel.class);

        binding = FragmentWriteviewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        final TextView textView = binding.writeview;
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        //  Write view action bar에 날짜 찍히게  - 이 주석 지우고 프래그먼트 왔다갔다하면 무슨 코드인지 Write view 상단 actionbar에서 알 수 있음
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
//        String currentDate = sdf.format(new Date());
//        requireActivity().setTitle(currentDate);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}