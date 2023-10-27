package snu.swpp.moment.ui.main_writeview;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import snu.swpp.moment.data.callback.MomentGetCallBack;
import snu.swpp.moment.data.callback.MomentWriteCallBack;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.MomentPairModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;
import snu.swpp.moment.utils.TimeConverter;

public class TodayViewModel extends ViewModel {

    private final long MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;
    private final int REFRESH_TOKEN_EXPIRED = 1;
    private final MutableLiveData<MomentUiState> momentState = new MutableLiveData<>();
    private final AuthenticationRepository authenticationRepository;
    private final MomentRepository momentRepository;

    public TodayViewModel(AuthenticationRepository authenticationRepository,
        MomentRepository momentRepository) {
        this.authenticationRepository = authenticationRepository;
        this.momentRepository = momentRepository;
    }

    public LiveData<MomentUiState> getMomentState() {
        return momentState;
    }

    public void getMoment(int year, int month, int date) {
        StartAndEndDateInLong startEnd = getStartAndEndInLong(year, month, date);
        long start = startEnd.getStart();
        long end = startEnd.getEnd();

        authenticationRepository.isTokenValid(new TodayViewModel.WriteViewTokenCallback() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                momentRepository.getMoment(access_token, start, end, new MomentGetCallBack() {
                    @Override
                    public void onSuccess(ArrayList<MomentPairModel> momentPair) {
                        momentState.setValue(
                            new MomentUiState(-1, momentPair)
                        );
                    }

                    @Override
                    public void onFailure(int error) {
                        momentState.setValue(
                            new MomentUiState(error, new ArrayList<>())
                        );
                    }
                });
            }
        });
    }

    public void writeMoment(String moment) {
        authenticationRepository.isTokenValid(new WriteViewTokenCallback() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                momentRepository.writeMoment(access_token, moment,
                    new MomentWriteCallBack() {
                        @Override
                        public void onSuccess(MomentPairModel momentPair) {
                            ArrayList<MomentPairModel> reply = momentState.getValue()
                                .getMomentPairsList();
                            reply.add(momentPair);
                            momentState.setValue(new MomentUiState(-1, reply));
                        }

                        @Override
                        public void onFailure(int error) {
                            momentState.setValue(
                                new MomentUiState(error, null)
                            );
                        }
                    });
            }
        });
    }

    private StartAndEndDateInLong getStartAndEndInLong(int year, int month, int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, date, 3, 0, 0);  // month is 0-based
        Date startDate = calendar.getTime();

        calendar.add(Calendar.MILLISECOND, (int) (MILLIS_IN_A_DAY - 1));
        Date endDate = calendar.getTime();
        long start = TimeConverter.convertDateToLong(startDate);
        long end = TimeConverter.convertDateToLong(endDate);
        return new StartAndEndDateInLong(start, end);
    }

    abstract class WriteViewTokenCallback implements TokenCallBack {

        @Override
        public void onFailure() {
            momentState.setValue(new MomentUiState(REFRESH_TOKEN_EXPIRED, null));
        }
    }

    private static class StartAndEndDateInLong {

        private final long start;
        private final long end;

        public StartAndEndDateInLong(long start, long end) {
            this.start = start;
            this.end = end;
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }
    }
}