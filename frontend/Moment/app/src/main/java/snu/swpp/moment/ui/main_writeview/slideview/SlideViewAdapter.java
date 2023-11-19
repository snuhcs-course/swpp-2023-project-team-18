package snu.swpp.moment.ui.main_writeview.slideview;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;


public class SlideViewAdapter extends FragmentStateAdapter {

    public int count;

    public SlideViewAdapter(Fragment fa, int count) {
        super(fa);
        this.count = count;
    }

    @NonNull
    @Override
    public BaseWritePageFragment createFragment(int position) {
        int index = getRealPosition(position);
        if (index == count - 1) {
            return new TodayViewFragment();
        } else {
            return DailyViewFragment.initialize(count - index - 1);
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