package snu.swpp.moment.ui.main_writeview;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import snu.swpp.moment.LoginRegisterActivity;
import snu.swpp.moment.MainActivity;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.databinding.FragmentWriteviewBinding;
import snu.swpp.moment.ui.main_writeview.slideview.SlideViewAdapter;
import snu.swpp.moment.utils.TimeConverter;


public class WriteViewFragment extends Fragment {

    private final int DEFAULT_PAGE = 100;
    private int num_page = DEFAULT_PAGE;
    private FragmentWriteviewBinding binding;

    // ViewPager variables
    private ViewPager2 mPager;
    private FragmentStateAdapter pagerAdapter;
    private final Handler slideHandler = new Handler(); // 슬라이드를 자동으로 변경하는 Handler

    private AuthenticationRepository authenticationRepository;

    public View onCreateView(@NonNull LayoutInflater inflater,
        ViewGroup container, Bundle savedInstanceState) {

        try {
            authenticationRepository = AuthenticationRepository.getInstance(getContext());
        } catch (Exception e) {
            Toast.makeText(getContext(), "알 수 없는 인증 오류", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), LoginRegisterActivity.class);
            startActivity(intent);
        }

        initializeNumPage();

        binding = FragmentWriteviewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 여기부터 slide가 가능해짐
        mPager = binding.viewpager;
        pagerAdapter = new SlideViewAdapter(WriteViewFragment.this, num_page);
        mPager.setAdapter(pagerAdapter);
        mPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        // 최초의 페이지 설정은 이거로 함 (numpage 보다 크면 마지막페이지가 세팅되는듯 - 아래 onPageChangeCallBack)
        mPager.setCurrentItem(num_page, false);
        // offscreen 몇 페이지가 로드되어 있을지 설정 (Latency 감소)
        mPager.setOffscreenPageLimit(3);

        mPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);

                if (positionOffsetPixels == 0) {
                    int index = position;
                    if (position >= num_page) { // 마지막 페이지에서 오른쪽으로 넘어갈 때 마지막 페이지로 고정
                        index = num_page - 1;
                        mPager.setCurrentItem(num_page - 1, false);
                    } else if (position < 0) { // 첫 페이지에서 왼쪽으로 넘어갈 때 첫 페이지로 고정
                        index = 0;
                        mPager.setCurrentItem(0, false);
                    }
                    LocalDate pageDate = TimeConverter.getToday().minusDays(num_page - index - 1);
                    String formattedDate = TimeConverter.formatLocalDate(pageDate, "yyyy. MM. dd.");
                    MainActivity activity = (MainActivity) getActivity();
                    activity.setToolbarTitle(formattedDate);
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                //mIndicator.animatePageSelected(position % 4);
            }
        });

        return root;
    }

    private void initializeNumPage() {
        int hour;
        LocalDate created_at, today;
        String dateInString = authenticationRepository.getCreatedAt();
        if (dateInString.isBlank()) {
            return;
        }
        today = TimeConverter.getToday();
        created_at = LocalDate.parse(dateInString.substring(0, 10));
        hour = Integer.parseInt(dateInString.substring(11, 13));
        created_at = TimeConverter.updateDateFromThree(created_at, hour);

        int dayDiff = (int) ChronoUnit.DAYS.between(created_at, today);
        this.num_page = dayDiff + 1;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}