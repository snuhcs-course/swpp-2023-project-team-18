package snu.swpp.moment.ui.main_writeview;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import snu.swpp.moment.data.callback.StoryGetCallBack;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.HashtagModel;
import snu.swpp.moment.data.model.StoryModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.StoryRepository;
import snu.swpp.moment.exception.UnauthorizedAccessException;
import snu.swpp.moment.ui.main_writeview.uistate.StoryUiState;
import snu.swpp.moment.utils.EmotionMap;
import snu.swpp.moment.utils.TimeConverter;

public class GetStoryUseCase {

    private final MutableLiveData<StoryUiState> storyState = new MutableLiveData<>();
    private final AuthenticationRepository authenticationRepository;
    private final StoryRepository storyRepository;
    private int storyId = -1;

    public GetStoryUseCase(
        AuthenticationRepository authenticationRepository,
        StoryRepository storyRepository
    ) {
        this.authenticationRepository = authenticationRepository;
        this.storyRepository = storyRepository;
    }

    public void getStory(LocalDateTime now) {
        long[] dayInterval = TimeConverter.getOneDayIntervalTimestamps(now);

        authenticationRepository.isTokenValid(new TokenCallBack() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                storyRepository.getStory(access_token, dayInterval[0], dayInterval[1],
                    new StoryGetCallBack() {
                        @Override
                        public void onSuccess(List<StoryModel> story) {
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
                                boolean isPointCompleted = storyInstance.getIsPointCompleted();

                                storyId = storyInstance.getId();

                                storyState.setValue(
                                    new StoryUiState(null, false, title, content, emotion,
                                        parsedTags,
                                        score,
                                        createdAt,
                                        isPointCompleted));
                            }
                        }

                        @Override
                        public void onFailure(Exception error) {
                            storyState.setValue(StoryUiState.withError(error));
                        }
                    });
            }

            @Override
            public void onFailure() {
                storyState.setValue(StoryUiState.withError(new UnauthorizedAccessException()));
            }
        });
    }

    public int getStoryId() {
        if (storyId == -1) {
            Log.d("GetStoryUseCase", "StoryId is not set yet (id=-1)");
        }
        return storyId;
    }

    public void setStoryId(int storyId) {
        this.storyId = storyId;
    }

    public void observeStoryState(Observer<StoryUiState> observer) {
        storyState.observeForever(observer);
    }
}
