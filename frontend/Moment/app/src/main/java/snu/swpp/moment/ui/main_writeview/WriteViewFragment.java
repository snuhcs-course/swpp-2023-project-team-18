package snu.swpp.moment.ui.main_writeview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import snu.swpp.moment.ui.main_writeview.DaySlide.DailyViewAdapter;
import snu.swpp.moment.databinding.FragmentWriteviewBinding;


public class WriteViewFragment extends Fragment {

    private FragmentWriteviewBinding binding;
    // ViewPager variables
    private ViewPager2 mPager;
    // below tow indicator is not used. this is circle you can see in instagram
    private FragmentStateAdapter pagerAdapter;
    private int num_page = 4;
    private Handler slideHandler = new Handler(); // 슬라이드를 자동으로 변경하는 Handler
    //private CircleIndicator3 mIndicator;
    // ViewPager variables end


    public View onCreateView(@NonNull LayoutInflater inflater,
        ViewGroup container, Bundle savedInstanceState) {
        WriteViewModel homeViewModel =
            new ViewModelProvider(this).get(WriteViewModel.class);

        binding = FragmentWriteviewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        //final TextView textView = binding.writeview;
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        //  Write view action bar에 날짜 찍히게  - 이 주석 지우고 프래그먼트 왔다갔다하면 무슨 코드인지 Write view 상단 actionbar에서 알 수 있음
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
//        String currentDate = sdf.format(new Date());
//        requireActivity().setTitle(currentDate);




        // TODO : 여기부터 slide가 가능해짐
        //ViewPager2
        mPager = binding.viewpager;
        //Adapter
        pagerAdapter = new DailyViewAdapter(WriteViewFragment.this, num_page);
        mPager.setAdapter(pagerAdapter);
        //Indicator
        //        mIndicator = binding.indicator;
        //        mIndicator.setViewPager(mPager);
        //        mIndicator.createIndicators(num_page,0);
        //ViewPager Setting

        mPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        // 최초의 페이지 설정은 이거로함 (numpage 보다 크면 마지막페이지가 세팅되는듯 - 아래 onPageChangeCallBack)
        mPager.setCurrentItem(4);

        //offscree 몇페이지가 로드되어있을지 설정 (Latency 감소)
        mPager.setOffscreenPageLimit(3);

        mPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);

                if (positionOffsetPixels == 0) {
                    if (position > 3) { // 마지막 페이지 3으로 설정
                        mPager.setCurrentItem(3, false);
                    } else if (position < 0) { // 첫 페이지 0으로 설정
                        mPager.setCurrentItem(0, false);
                    }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}