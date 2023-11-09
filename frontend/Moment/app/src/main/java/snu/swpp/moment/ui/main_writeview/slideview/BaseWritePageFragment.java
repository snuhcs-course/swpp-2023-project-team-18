package snu.swpp.moment.ui.main_writeview.slideview;

import android.os.Handler;
import android.util.Log;
import androidx.fragment.app.Fragment;
import java.time.LocalDateTime;
import snu.swpp.moment.MainActivity;

public abstract class BaseWritePageFragment extends Fragment {

    protected LocalDateTime lastRefreshedTime = LocalDateTime.now();
    private final Handler refreshHandler = new Handler();
    private final long REFRESH_INTERVAL = 1000 * 60 * 5;  // 5 minutes
    private final int STARTING_HOUR = 3;

    @Override
    public void onResume() {
        Log.d("BaseWritePageFragment", "onResume: Called");
        Log.d("BaseWritePageFragment", "onResume: lastRefreshedTime: " + lastRefreshedTime);
        super.onResume();
        setToolbarTitle();
        if (isOutdated()) {
            Log.d("BaseWritePageFragment", "onResume: Outdated, call APIs to refresh");
            callApisToRefresh();
            updateRefreshTime();
        }
    }

    public void setToolbarTitle() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.setToolbarTitle(getDateText());
    }

    protected abstract void callApisToRefresh();

    protected abstract String getDateText();

    protected void updateRefreshTime() {
        lastRefreshedTime = LocalDateTime.now();
    }

    /**
     * REFRESH_INTERVAL 후에 runnable을 실행함
     */
    protected void registerRefreshRunnable(Runnable runnable) {
        refreshHandler.postDelayed(runnable, REFRESH_INTERVAL);
    }

    protected boolean isOutdated() {
        LocalDateTime now = LocalDateTime.now();
        if (lastRefreshedTime.getDayOfMonth() == now.getDayOfMonth()) {
            return false;
        }
        return now.getHour() >= STARTING_HOUR;
    }
}
