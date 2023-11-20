package snu.swpp.moment.ui.main_writeview.slideview;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import java.time.LocalDate;
import java.time.LocalDateTime;
import snu.swpp.moment.LoginRegisterActivity;
import snu.swpp.moment.MainActivity;
import snu.swpp.moment.R;
import snu.swpp.moment.exception.NoInternetException;
import snu.swpp.moment.exception.UnauthorizedAccessException;
import snu.swpp.moment.ui.main_writeview.uistate.MomentUiState;
import snu.swpp.moment.ui.main_writeview.uistate.StoryUiState;
import snu.swpp.moment.ui.main_writeview.viewmodel.WritePageDataUnitFactory;
import snu.swpp.moment.utils.TimeConverter;

public abstract class BaseWritePageFragment extends Fragment {

    protected WritePageDataUnitFactory dataUnitFactory;
    protected final ApiResponseManager apiResponseManager = new ApiResponseManager();

    protected LocalDateTime lastRefreshedTime = getCurrentDateTime();
    private final Handler refreshHandler = new Handler();
    private final long REFRESH_INTERVAL = 1000 * 60 * 5;  // 5 minutes

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        if (dataUnitFactory == null) {
            dataUnitFactory = new WritePageDataUnitFactory(requireContext());
        }
        return null;
    }

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

    protected void handleApiError(Exception error) {
        if (error instanceof NoInternetException) {
            Log.d("BaseWritePageFragment", "Refreshing in handleApiError");
            Toast.makeText(requireContext(), R.string.internet_error, Toast.LENGTH_SHORT)
                .show();
            callApisToRefresh();
            updateRefreshTime();
        } else if (error instanceof UnauthorizedAccessException) {
            Toast.makeText(requireContext(), R.string.token_expired_error, Toast.LENGTH_SHORT)
                .show();
            Intent intent = new Intent(requireContext(), LoginRegisterActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), R.string.unknown_error, Toast.LENGTH_SHORT).show();
        }
    }

    protected static class ApiResponseManager {

        MomentUiState momentUiState = null;
        StoryUiState storyUiState = null;
        ApiResponseProcessor processor = null;

        void saveResponse(MomentUiState momentUiState) {
            this.momentUiState = momentUiState;
        }

        void saveResponse(StoryUiState storyUiState) {
            this.storyUiState = storyUiState;
        }

        void reset() {
            momentUiState = null;
            storyUiState = null;
        }

        void registerProcessor(ApiResponseProcessor processor) {
            this.processor = processor;
        }

        void process() {
            if (momentUiState == null || storyUiState == null) {
                return;
            }
            processor.processApiResponse(momentUiState, storyUiState);
        }
    }

    interface ApiResponseProcessor {

        void processApiResponse(MomentUiState momentUiState, StoryUiState storyUiState);
    }
}

