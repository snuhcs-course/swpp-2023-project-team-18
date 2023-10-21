package snu.swpp.moment.ui.main_writeview;

import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import snu.swpp.moment.data.callback.MomentGetCallBack;
import snu.swpp.moment.data.callback.MomentWriteCallBack;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.MomentPair;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;
import snu.swpp.moment.utils.TimeConverter;

public class WriteViewModel extends ViewModel {

    private final long MILLIS_IN_A_DAY = 1000*60*60*24;
    private final int REFRESH_TOKEN_EXPIRED = 1;
    private final MutableLiveData<MomentUiState> momentState = new MutableLiveData<>();
    private AuthenticationRepository authenticationRepository;
    private MomentRepository momentRepository;

    public WriteViewModel(AuthenticationRepository authenticationRepository, MomentRepository momentRepository) {
        this.authenticationRepository = authenticationRepository;
        this.momentRepository = momentRepository;
    }

    public LiveData<MomentUiState> getMomentState() {
        return momentState;
    }
    public void setMomentState(MomentUiState momentState){ this.momentState.setValue(momentState);}

    public void getMoment(int year, int month, int date) {
        //Date startDate = new Date(year, month, date, 3, 0);
        //Date endDate = new Date(startDate.getTime()+MILLIS_IN_A_DAY-1);
        //long start = new Timestamp(startDate.getTime()).getTime();
        //long end = new Timestamp(endDate.getTime()).getTime();
        //long start = 1697808500;
        //long end = 1697808999;
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, date, 3, 0, 0);  // month is 0-based
        Date startDate = calendar.getTime();

        calendar.add(Calendar.MILLISECOND, (int) (MILLIS_IN_A_DAY - 1));
        Date endDate = calendar.getTime();
        long start = TimeConverter.convertDateToLong(startDate);
        long end = TimeConverter.convertDateToLong(endDate);

        System.out.println("#Debug  start : "+ start + " end : "+end );


        authenticationRepository.isTokenValid(new TokenCallBack() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                momentRepository.getMoment(access_token, start, end, new MomentGetCallBack() {
                    @Override
                    public void onSuccess(ArrayList<MomentPair> momentPair) {
                        //System.out.println("#DEBUG: VIEWMODEL " + momentPair.size());
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

            @Override
            public void onFailure() {
                momentState.setValue(new MomentUiState(REFRESH_TOKEN_EXPIRED, null));
            }
        });
    }

    public void writeMoment(String moment){
        String writtenMoment = moment;
        authenticationRepository.isTokenValid(new TokenCallBack() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                momentRepository.writeMoment(access_token, writtenMoment, new MomentWriteCallBack() {
                    @Override
                    public void onSuccess(MomentPair momentPair) {
                        ArrayList<MomentPair> reply = momentState.getValue().getMomentPairsList();
                        reply.add(momentPair);
                        momentState.setValue(new MomentUiState(-1, reply));
                    }

                    @Override
                    public void onFailure(int error) {
                        momentState.setValue(
                                //new MomentUiState(error, new ArrayList<>())
                                new MomentUiState(error, null)
                        );
                    }
                });
            }

            @Override
            public void onFailure() {
                momentState.setValue(new MomentUiState(REFRESH_TOKEN_EXPIRED, null));
            }
        });

    }
}