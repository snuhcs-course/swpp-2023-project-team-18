package snu.swpp.moment.ui.main_writeview;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import snu.swpp.moment.data.callback.MomentGetCallBack;
import snu.swpp.moment.data.callback.StoryGetCallBack;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.HashtagModel;
import snu.swpp.moment.data.model.MomentPairModel;
import snu.swpp.moment.data.model.StoryModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;
import snu.swpp.moment.data.repository.StoryRepository;
import snu.swpp.moment.ui.main_writeview.uistate.StoryUiState;
import snu.swpp.moment.ui.main_writeview.uistate.MomentUiState;
import snu.swpp.moment.utils.EmotionMap;
import snu.swpp.moment.utils.TimeConverter;

public class DailyViewModel extends ViewModel {

    private final int REFRESH_TOKEN_EXPIRED = 1;
    private final MutableLiveData<MomentUiState> momentState = new MutableLiveData<>();
    private final MutableLiveData<StoryUiState> storyState = new MutableLiveData<>();
    private final AuthenticationRepository authenticationRepository;
    private final MomentRepository momentRepository;
    private final StoryRepository storyRepository;

    public DailyViewModel(
        AuthenticationRepository authenticationRepository,
        MomentRepository momentRepository,
        StoryRepository storyRepository
    ) {
        this.authenticationRepository = authenticationRepository;
        this.momentRepository = momentRepository;
        this.storyRepository = storyRepository;
    }

    public void getMoment(int year, int month, int date) {
        long[] dayInterval = TimeConverter.getOneDayIntervalTimestamps(year, month, date);

        authenticationRepository.isTokenValid(new WriteViewTokenCallback() {
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

    public void getStory(int year, int month, int date) {
        long[] dayInterval = TimeConverter.getOneDayIntervalTimestamps(year, month, date);

        authenticationRepository.isTokenValid(new WriteViewTokenCallback() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                storyRepository.getStory(access_token, dayInterval[0], dayInterval[1],
                    new StoryGetCallBack() {
                        @Override
                        public void onSuccess(ArrayList<StoryModel> story) {
                            if (story.isEmpty()) {
                                storyState.setValue(StoryUiState.empty());
                            } else {
                                StoryModel storyInstance = story.get(0);
                                String title = storyInstance.getTitle();
                                String content = storyInstance.getContent();
                                int emotion = EmotionMap.getEmotionInt(storyInstance.getEmotion());
                                List<HashtagModel> tags = storyInstance.getHashtags();
                                List<String> parsedTags = new ArrayList<>();
                                for (HashtagModel hashtagModel : tags) {
                                    parsedTags.add(hashtagModel.getContent());
                                }
                                int score = storyInstance.getScore();
                                Date createdAt = storyInstance.getCreatedAt();

                                storyState.setValue(
                                    new StoryUiState(null, false, title, content, emotion,
                                        parsedTags,
                                        score,
                                        createdAt));
                            }
                        }

                        @Override
                        public void onFailure(Exception error) {
                            storyState.setValue(StoryUiState.empty());
                        }
                    });
            }
        });
    }

    public void observeMomentState(Observer<MomentUiState> observer) {
        momentState.observeForever(observer);
    }

    public void observeStoryState(Observer<StoryUiState> observer) {
        storyState.observeForever(observer);
    }

    abstract class WriteViewTokenCallback implements TokenCallBack {

        @Override
        public void onFailure() {
            momentState.setValue(new MomentUiState(REFRESH_TOKEN_EXPIRED, null));
        }
    }
}
