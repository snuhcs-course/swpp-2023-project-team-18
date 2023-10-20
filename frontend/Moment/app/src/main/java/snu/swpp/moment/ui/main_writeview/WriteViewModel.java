package snu.swpp.moment.ui.main_writeview;

import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import snu.swpp.moment.data.callback.MomentGetCallBack;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.MomentPair;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;

public class WriteViewModel extends ViewModel {

    private final long MILLIS_IN_A_DAY = 1000*60*60*24;
    private final int REFRESH_TOKEN_EXPIRED = 1;
    private final MutableLiveData<ArrayList<MomentPair>> momentState = new MutableLiveData<>();
    private MutableLiveData<Integer> errorState = new MutableLiveData<>();
    private AuthenticationRepository authenticationRepository;
    private MomentRepository momentRepository;

    public WriteViewModel(AuthenticationRepository authenticationRepository, MomentRepository momentRepository) {
        this.authenticationRepository = authenticationRepository;
        this.momentRepository = momentRepository;
    }

    public LiveData<ArrayList<MomentPair>> getMomentState() {
        return momentState;
    }
    public LiveData<Integer> getErrorState() { return errorState; }

    public void getMoment(int year, int month, int date) {
        Date startDate = new Date(year, month, date, 3, 0);
        Date endDate = new Date(startDate.getTime()+MILLIS_IN_A_DAY-1);
        //long start = new Timestamp(startDate.getTime()).getTime();
        //long end = new Timestamp(endDate.getTime()).getTime();
        long start = 1697808500;
        long end = 1697808999;

        authenticationRepository.isTokenValid(new TokenCallBack() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                momentRepository.getMoment(access_token, start, end, new MomentGetCallBack() {
                    @Override
                    public void onSuccess(ArrayList<MomentPair> momentPair) {
                        System.out.println("#DEBUG: VIEWMODEL " + momentPair.size());
                        momentState.setValue(momentPair);
                    }

                    @Override
                    public void onFailure(int error) {
                        errorState.setValue(error);
                    }
                });
            }

            @Override
            public void onFailure() {
                errorState.setValue(REFRESH_TOKEN_EXPIRED);
            }
        });
    }
}