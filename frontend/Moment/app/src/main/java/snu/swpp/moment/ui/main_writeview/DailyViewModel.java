package snu.swpp.moment.ui.main_writeview;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import java.time.LocalDateTime;
import java.util.List;
import snu.swpp.moment.data.callback.MomentGetCallBack;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.MomentPairModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;
import snu.swpp.moment.exception.UnauthorizedAccessException;
import snu.swpp.moment.ui.main_writeview.uistate.MomentUiState;
import snu.swpp.moment.ui.main_writeview.uistate.StoryUiState;
import snu.swpp.moment.utils.TimeConverter;

public class DailyViewModel extends ViewModel {

    private final GetStoryUseCase getStoryUseCase;
    private final SaveScoreUseCase saveScoreUseCase;
    private final MutableLiveData<MomentUiState> momentState = new MutableLiveData<>();
    private final AuthenticationRepository authenticationRepository;
    private final MomentRepository momentRepository;

    public DailyViewModel(
        AuthenticationRepository authenticationRepository,
        MomentRepository momentRepository,
        GetStoryUseCase getStoryUseCase,
        SaveScoreUseCase saveScoreUseCase
    ) {
        this.authenticationRepository = authenticationRepository;
        this.momentRepository = momentRepository;
        this.getStoryUseCase = getStoryUseCase;
        this.saveScoreUseCase = saveScoreUseCase;
    }

    public void getMoment(LocalDateTime now) {
        long[] dayInterval = TimeConverter.getOneDayIntervalTimestamps(now);
        System.out.println("#DAYS " + now + ": " + dayInterval[0] + " " + dayInterval[1]);

        authenticationRepository.isTokenValid(new TokenCallBack() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                momentRepository.getMoment(access_token, dayInterval[0], dayInterval[1],
                    new MomentGetCallBack() {
                        @Override
                        public void onSuccess(List<MomentPairModel> momentPair) {
                            momentState.setValue(
                                new MomentUiState(null, momentPair)
                            );
                        }

                        @Override
                        public void onFailure(Exception error) {
                            momentState.setValue(MomentUiState.withError(error));
                        }
                    });
            }

            @Override
            public void onFailure() {
                momentState.setValue(MomentUiState.withError(new UnauthorizedAccessException()));
            }
        });
    }

    public void getStory(LocalDateTime now) {
        getStoryUseCase.getStory(now);
    }

    public void saveScore(int score) {
        int story_id = getStoryUseCase.getStoryId();
        if (story_id == -1) {
            Log.d("DailyViewModel", "saveScore: StoryId is not set yet (id=-1)");
            return;
        }
        saveScoreUseCase.saveScore(story_id, score);
    }

    public void observeMomentState(Observer<MomentUiState> observer) {
        momentState.observeForever(observer);
    }

    public void observeStoryState(Observer<StoryUiState> observer) {
        getStoryUseCase.observeStoryState(observer);
    }
}
