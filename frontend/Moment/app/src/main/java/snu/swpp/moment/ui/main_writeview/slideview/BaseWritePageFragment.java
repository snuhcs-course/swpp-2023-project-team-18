package snu.swpp.moment.ui.main_writeview.slideview;

import android.os.Handler;
import android.util.Log;
import androidx.fragment.app.Fragment;
import java.time.LocalDateTime;
import snu.swpp.moment.MainActivity;

public abstract class BaseWritePageFragment extends Fragment {

    protected final Handler refreshHandler = new Handler();
    protected LocalDateTime lastRefreshedTime = LocalDateTime.now();
    protected final long REFRESH_INTERVAL = 1000 * 60 * 10;   // 10 minutes
    protected final int STARTING_HOUR = 3;

    public void setToolbarTitle() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setToolbarTitle(getDateText());
        } else {
            throw new RuntimeException("MainActivity is null");
        }
    }

    public abstract String getDateText();

    protected void updateRefreshTime() {
        lastRefreshedTime = LocalDateTime.now();
    }

    protected boolean isOutdated() {
        LocalDateTime now = LocalDateTime.now();
        if (lastRefreshedTime.getDayOfMonth() == now.getDayOfMonth()) {
            return false;
        }
        return now.getHour() >= STARTING_HOUR;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("BaseWritePageFragment", "onResume");
        setToolbarTitle();
    }
}
