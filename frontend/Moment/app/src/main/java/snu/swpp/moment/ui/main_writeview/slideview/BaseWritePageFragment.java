package snu.swpp.moment.ui.main_writeview.slideview;

import android.os.Handler;
import android.util.Log;
import androidx.fragment.app.Fragment;
import java.time.LocalDate;
import java.time.LocalDateTime;
import snu.swpp.moment.MainActivity;
import snu.swpp.moment.utils.TimeConverter;

public abstract class BaseWritePageFragment extends Fragment {

    protected LocalDateTime lastRefreshedTime = getCurrentDateTime();
    private final Handler refreshHandler = new Handler();
    private final long REFRESH_INTERVAL = 1000 * 60 * 5;  // 5 minutes

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

    protected void setToolbarTitle() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.setToolbarTitle(getDateText());
    }

    protected abstract void callApisToRefresh();

    protected String getDateText() {
        LocalDate date = getCurrentDate();
        String dayOfWeek = date.getDayOfWeek().getDisplayName(
            java.time.format.TextStyle.SHORT, java.util.Locale.KOREAN);
        Log.d("BaseWritePageFragment", "getDateText: date: " + date + ", dayOfWeek: " + dayOfWeek);
        return TimeConverter.formatLocalDate(date, "yyyy. MM. dd.") + " " + dayOfWeek;
    }

    /**
     * 3시 기준으로 보정된 LocalDate
     */
    protected abstract LocalDate getCurrentDate();

    /**
     * API 호출 시 사용할 LocalDateTime
     */
    protected abstract LocalDateTime getCurrentDateTime();

    protected void updateRefreshTime() {
        lastRefreshedTime = getCurrentDateTime();
    }

    /**
     * REFRESH_INTERVAL 후에 runnable을 실행함
     */
    protected void registerRefreshRunnable(Runnable runnable) {
        refreshHandler.postDelayed(runnable, REFRESH_INTERVAL);
    }

    protected boolean isOutdated() {
        return TimeConverter.hasDayPassed(lastRefreshedTime, getCurrentDateTime());
    }
}
