package snu.swpp.moment.ui.main_writeview.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import snu.swpp.moment.data.callback.ScoreSaveCallback;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.StoryRepository;
import snu.swpp.moment.exception.UnauthorizedAccessException;
import snu.swpp.moment.ui.main_writeview.uistate.CompletionStoreResultState;

public class SaveScoreUseCase {

    private final MutableLiveData<CompletionStoreResultState> scoreResultState = new MutableLiveData<>();
    private final AuthenticationRepository authenticationRepository;
    private final StoryRepository storyRepository;

    public SaveScoreUseCase(AuthenticationRepository authenticationRepository,
        StoryRepository storyRepository) {
        this.authenticationRepository = authenticationRepository;
        this.storyRepository = storyRepository;
    }

    public void saveScore(int storyId, int score) {
        authenticationRepository.isTokenValid(new TokenCallBack() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                storyRepository.saveScore(access_token, storyId, score, new ScoreSaveCallback() {
                    @Override
                    public void onSuccess() {
                        scoreResultState.setValue(new CompletionStoreResultState(null));
                    }

                    @Override
                    public void onFailure(Exception error) {
                        scoreResultState.setValue(new CompletionStoreResultState(error));
                    }
                });
            }

            @Override
            public void onFailure() {
                scoreResultState.setValue(
                    new CompletionStoreResultState(new UnauthorizedAccessException()));
            }
        });
    }

    public void observeScoreResultState(Observer<CompletionStoreResultState> observer) {
        scoreResultState.observeForever(observer);
    }
}
