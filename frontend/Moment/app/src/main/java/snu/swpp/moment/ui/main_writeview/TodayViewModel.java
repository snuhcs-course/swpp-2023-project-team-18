package snu.swpp.moment.ui.main_writeview;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import snu.swpp.moment.data.callback.MomentGetCallBack;
import snu.swpp.moment.data.callback.MomentWriteCallBack;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.MomentPairModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;
import snu.swpp.moment.ui.main_writeview.uistate.MomentUiState;
import snu.swpp.moment.utils.TimeConverter;

public class TodayViewModel extends ViewModel {

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
        long[] dayInterval = TimeConverter.getOneDayIntervalTimestamps(year, month, date);

        authenticationRepository.isTokenValid(new TodayViewModel.WriteViewTokenCallback() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                momentRepository.getMoment(access_token, dayInterval[0], dayInterval[1],
                    new MomentGetCallBack() {
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

    abstract class WriteViewTokenCallback implements TokenCallBack {

        @Override
        public void onFailure() {
            momentState.setValue(new MomentUiState(REFRESH_TOKEN_EXPIRED, null));
        }
    }
}