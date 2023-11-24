package snu.swpp.moment.ui.main_writeview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import snu.swpp.moment.LoginRegisterActivity;
import snu.swpp.moment.MainActivity;
import snu.swpp.moment.data.factory.AuthenticationRepositoryFactory;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.databinding.FragmentWriteviewBinding;
import snu.swpp.moment.ui.main_writeview.slideview.SlideViewAdapter;
import snu.swpp.moment.utils.AnimationProvider;
import snu.swpp.moment.utils.TimeConverter;


public class WriteViewFragment extends Fragment {

    private FragmentWriteviewBinding binding;

    private SlideViewAdapter slideViewAdapter;
    private int numPages = -1;
    private boolean isInTodayPage = true;
    private final int OFF_SCREEN_PAGE_LIMIT = 3;

    private AuthenticationRepositoryFactory authenticationRepositoryFactory;
    private AuthenticationRepository authenticationRepository;

    private AnimationProvider animationProvider;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        authenticationRepositoryFactory = new AuthenticationRepositoryFactory(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
        ViewGroup container, Bundle savedInstanceState) {
        Log.d("WriteViewFragment", "onCreateView");

        try {
            authenticationRepository = authenticationRepositoryFactory.getRepository();
        } catch (RuntimeException e) {
            Toast.makeText(requireContext(), "알 수 없는 인증 오류", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireContext(), LoginRegisterActivity.class);
            startActivity(intent);
        }

        numPages = calculateNumPages();

        binding = FragmentWriteviewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.backToTodayButton.setActivated(true);
        animationProvider = new AnimationProvider(binding.backToTodayButton);

        slideViewAdapter = new SlideViewAdapter(WriteViewFragment.this, numPages);
        binding.viewpager.setAdapter(slideViewAdapter);
        binding.viewpager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        // 처음에 보여줄 페이지 설정
        binding.viewpager.setCurrentItem(numPages - 1, false);
        // 항상 로딩 상태로 둘 페이지 수 설정
        binding.viewpager.setOffscreenPageLimit(OFF_SCREEN_PAGE_LIMIT);

        // 오늘로 돌아가기 버튼
        binding.backToTodayButton.setOnClickListener(
            v -> binding.viewpager.setCurrentItem(numPages - 1, true));
        showBackToTodayButton(false, false);

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

                boolean newValue = position == numPages - 1;
                showBackToTodayButton(!newValue, isInTodayPage && !newValue);
                isInTodayPage = newValue;
            }
        });

        // 한달보기 탭에서 버튼을 눌러 넘어왔을 때 동작
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.observeWriteDestinationDate(localDate -> {
            Log.d("WriteViewFragment", "writeDestinationDateObserver: " + localDate);
            if (localDate == null) {
                return;
            }
            long minusDays = ChronoUnit.DAYS.between(localDate, TimeConverter.getToday());
            binding.viewpager.setCurrentItem(numPages - (int) minusDays - 1, true);
            mainActivity.resetWriteDestinationDate();
        });

        return root;
    }

    private int calculateNumPages() {
        LocalDate createdAt = authenticationRepository.getCreatedAt();
        LocalDate today = TimeConverter.getToday();

        return (int) ChronoUnit.DAYS.between(createdAt, today) + 1;
    }

    private void showBackToTodayButton(boolean show, boolean withAnimation) {
        if (show) {
            binding.backToTodayButton.setVisibility(View.VISIBLE);
            if (withAnimation) {
                binding.backToTodayButton.startAnimation(animationProvider.fadeIn);
            }
        } else {
            binding.backToTodayButton.setVisibility(View.GONE);
            binding.backToTodayButton.startAnimation(animationProvider.fadeOut);
        }
    }

    @Override
    public void onResume() {
        Log.d("WriteViewFragment", "onResume");
        super.onResume();
        showBackToTodayButton(!isInTodayPage, false);
    }

    @Override
    public void onDestroyView() {
        ((MainActivity) requireActivity()).unobserveWriteDestinationDate();
        super.onDestroyView();
    }
}