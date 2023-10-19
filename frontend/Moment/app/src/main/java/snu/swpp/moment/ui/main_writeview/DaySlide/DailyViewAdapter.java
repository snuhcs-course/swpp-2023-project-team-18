package snu.swpp.moment.ui.main_writeview.DaySlide;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;


public class DailyViewAdapter extends FragmentStateAdapter {

    public int mCount;

    public DailyViewAdapter(Fragment fa, int count) {
        super(fa);
        mCount = count;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        int index = getRealPosition(position);
        if (index == 3) return new DailyViewFragment();
        else if (index == 1) return new DailyViewFragment();
        else if (index == 2) return new DailyViewFragment();
        else return new DailyViewFragment();


    }

    // 전체 페이지수
    @Override
    public int getItemCount() {
        return 4;
    }

    public int getRealPosition(int position) {
        return position % mCount;
    }
}