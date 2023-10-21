package snu.swpp.moment.ui.main_writeview.DaySlide;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;


public class DailyViewAdapter extends FragmentStateAdapter {

    public int count;

    public DailyViewAdapter(Fragment fa, int count) {
        super(fa);
        this.count = count;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        int index = getRealPosition(position);
        if (index == count - 1) {
            return new TodayViewFragment();
        } else {
            return new DailyViewFragment();
        }


    }

    // 전체 페이지수
    @Override
    public int getItemCount() {
        return count;
    }

    public int getRealPosition(int position) {
        return position % count;
    }
}