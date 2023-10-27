package snu.swpp.moment.ui.main_writeview;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.Calendar;
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
import snu.swpp.moment.ui.main_writeview.DaySlide.StoryUiState;
import snu.swpp.moment.utils.EmotionMap;
import snu.swpp.moment.utils.TimeConverter;

public class DailyViewModel extends ViewModel {

    private final long MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;
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

    public LiveData<MomentUiState> getMomentState() {
        return momentState;
    }

    public LiveData<StoryUiState> getStoryState() {
        return storyState;
    }

    public void getMoment(int year, int month, int date) {
        StartAndEndDateInLong startEnd = getStartAndEndInLong(year, month, date);
        long start = startEnd.getStart();
        long end = startEnd.getEnd();

        authenticationRepository.isTokenValid(new WriteViewTokenCallback() {
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

    public void getStory(int year, int month, int date) {
        StartAndEndDateInLong startEnd = getStartAndEndInLong(year, month, date);
        long start = startEnd.getStart();
        long end = startEnd.getEnd();

        authenticationRepository.isTokenValid(new WriteViewTokenCallback() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                storyRepository.getStory(access_token, start, end, new StoryGetCallBack() {
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
                                new StoryUiState(null, false, title, content, emotion, parsedTags,
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

    private class StartAndEndDateInLong {

        private long start;
        private long end;

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
