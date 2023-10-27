package snu.swpp.moment.ui.main_writeview;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import snu.swpp.moment.ui.main_writeview.DaySlide.DailyViewFragment;
import snu.swpp.moment.ui.main_writeview.DaySlide.TodayViewFragment;


public class SlideViewAdapter extends FragmentStateAdapter {

    public int count;

    public SlideViewAdapter(Fragment fa, int count) {
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