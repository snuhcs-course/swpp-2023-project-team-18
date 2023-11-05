package snu.swpp.moment.ui.main_writeview;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import snu.swpp.moment.LoginRegisterActivity;
import snu.swpp.moment.MainActivity;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.databinding.FragmentWriteviewBinding;
import snu.swpp.moment.ui.main_writeview.slideview.SlideViewAdapter;
import snu.swpp.moment.utils.AnimationProvider;
import snu.swpp.moment.utils.TimeConverter;


public class WriteViewFragment extends Fragment {

    private FragmentWriteviewBinding binding;

    private SlideViewAdapter slideViewAdapter;
    private int numPages = -1;
    private boolean isInTodayView;
    private final int OFF_SCREEN_PAGE_LIMIT = 3;

    private AuthenticationRepository authenticationRepository;

    private AnimationProvider animationProvider;

    public View onCreateView(@NonNull LayoutInflater inflater,
        ViewGroup container, Bundle savedInstanceState) {
        Log.d("WriteViewFragment", "onCreateView");

        try {
            authenticationRepository = AuthenticationRepository.getInstance(getContext());
        } catch (Exception e) {
            Toast.makeText(getContext(), "알 수 없는 인증 오류", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), LoginRegisterActivity.class);
            startActivity(intent);
        }

        numPages = calculateNumPages();

        binding = FragmentWriteviewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        slideViewAdapter = new SlideViewAdapter(WriteViewFragment.this, numPages);
        binding.viewpager.setAdapter(slideViewAdapter);
        binding.viewpager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        // 처음에 보여줄 페이지 설정
        binding.viewpager.setCurrentItem(numPages, false);
        binding.backToTodayButton.setVisibility(View.GONE);
        animationProvider = new AnimationProvider(binding.backToTodayButton);
        isInTodayView = true;
        // 항상 로딩 상태로 둘 페이지 수 설정
        binding.viewpager.setOffscreenPageLimit(OFF_SCREEN_PAGE_LIMIT);

        binding.viewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);

                if (positionOffsetPixels == 0) {
                    if (position >= numPages) { // 마지막 페이지에서 오른쪽으로 넘어갈 때 마지막 페이지로 고정
                        binding.viewpager.setCurrentItem(numPages - 1, false);
                    } else if (position < 0) { // 첫 페이지에서 왼쪽으로 넘어갈 때 첫 페이지로 고정
                        binding.viewpager.setCurrentItem(0, false);
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (position == numPages - 1) {
                    isInTodayView = true;
                    binding.backToTodayButton.setVisibility(View.GONE);
                } else if (isInTodayView && (position == numPages - 2)) {
                    binding.backToTodayButton.setVisibility(View.VISIBLE);
                    binding.backToTodayButton.startAnimation(animationProvider.fadeIn);
                    binding.backToTodayButton.setActivated(true);
                    isInTodayView = false;
                } else {
                    isInTodayView = false;
                    binding.backToTodayButton.setVisibility(View.VISIBLE);
                    binding.backToTodayButton.setActivated(true);
                }
            }
        });

        binding.backToTodayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.viewpager.setCurrentItem(numPages - 1, true);
            }
        });

        // 한달보기 탭에서 버튼을 눌러 넝머왔을 때 동작
        ((MainActivity) getActivity()).observeWriteDestinationDate(localDate -> {
            Log.d("WriteViewFragment", "observeWriteDestinationDate: " + localDate);
            Log.d("WriteViewFragment", "viewpager: " + binding.viewpager);
            long minusDays = ChronoUnit.DAYS.between(
                localDate, TimeConverter.getToday()
            );
            binding.viewpager.setCurrentItem(numPages - (int) minusDays - 1, true);
        });

        return root;
    }

    private int calculateNumPages() {
        int hour;
        LocalDate created_at, today;
        String dateInString = authenticationRepository.getCreatedAt();
        if (dateInString.isBlank()) {
            return -1;
        }
        today = TimeConverter.getToday();
        created_at = LocalDate.parse(dateInString.substring(0, 10));
        hour = Integer.parseInt(dateInString.substring(11, 13));
        created_at = TimeConverter.updateDateFromThree(created_at, hour);

        return (int) ChronoUnit.DAYS.between(created_at, today) + 1;
    }
}