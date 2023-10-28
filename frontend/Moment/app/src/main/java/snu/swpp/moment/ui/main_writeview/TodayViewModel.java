package snu.swpp.moment.ui.main_writeview;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import snu.swpp.moment.data.callback.MomentGetCallBack;
import snu.swpp.moment.data.callback.MomentWriteCallBack;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.MomentPairModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;
import snu.swpp.moment.ui.main_writeview.uistate.CompletionState;
import snu.swpp.moment.ui.main_writeview.uistate.CompletionStoreResultState;
import snu.swpp.moment.ui.main_writeview.uistate.MomentUiState;
import snu.swpp.moment.utils.TimeConverter;

public class TodayViewModel extends ViewModel {

    // 모먼트 작성
    private final MutableLiveData<MomentUiState> momentState = new MutableLiveData<>();
    private final AuthenticationRepository authenticationRepository;
    private final MomentRepository momentRepository;

    // 하루 마무리
    private final MutableLiveData<CompletionState> completionState = new MutableLiveData<>();
    private final MutableLiveData<CompletionStoreResultState> storyResultState = new MutableLiveData<>();
    private final MutableLiveData<CompletionStoreResultState> emotionResultState = new MutableLiveData<>();
    private final MutableLiveData<CompletionStoreResultState> tagsResultState = new MutableLiveData<>();
    private final MutableLiveData<CompletionStoreResultState> scoreResultState = new MutableLiveData<>();

    private final int REFRESH_TOKEN_EXPIRED = 1;

    public TodayViewModel(AuthenticationRepository authenticationRepository,
        MomentRepository momentRepository) {
        this.authenticationRepository = authenticationRepository;
        this.momentRepository = momentRepository;
    }

    public MomentUiState getMomentState() {
        return momentState.getValue();
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

    public void observeMomentState(Observer<MomentUiState> observer) {
        momentState.observeForever(observer);
    }

    public void observerCompletionState(Observer<CompletionState> observer) {
        completionState.observeForever(observer);
    }

    public void observerStoryResultState(Observer<CompletionStoreResultState> observer) {
        storyResultState.observeForever(observer);
    }

    public void observerEmotionResultState(Observer<CompletionStoreResultState> observer) {
        emotionResultState.observeForever(observer);
    }

    public void observerTagsResultState(Observer<CompletionStoreResultState> observer) {
        tagsResultState.observeForever(observer);
    }

    public void observerScoreResultState(Observer<CompletionStoreResultState> observer) {
        scoreResultState.observeForever(observer);
    }


    abstract class WriteViewTokenCallback implements TokenCallBack {

        @Override
        public void onFailure() {
            momentState.setValue(new MomentUiState(REFRESH_TOKEN_EXPIRED, null));
        }
    }
}