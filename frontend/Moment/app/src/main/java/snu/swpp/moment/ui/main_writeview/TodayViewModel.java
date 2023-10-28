package snu.swpp.moment.ui.main_writeview;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import java.time.LocalDate;
import java.util.ArrayList;
import snu.swpp.moment.api.response.AIStoryGetResponse;
import snu.swpp.moment.api.response.StoryCompletionNotifyResponse;
import snu.swpp.moment.data.callback.AIStoryCallback;
import snu.swpp.moment.data.callback.EmotionSaveCallback;
import snu.swpp.moment.data.callback.HashtagSaveCallback;
import snu.swpp.moment.data.callback.MomentGetCallBack;
import snu.swpp.moment.data.callback.MomentWriteCallBack;
import snu.swpp.moment.data.callback.ScoreSaveCallback;
import snu.swpp.moment.data.callback.StoryCompletionNotifyCallBack;
import snu.swpp.moment.data.callback.StorySaveCallback;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.MomentPairModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;
import snu.swpp.moment.data.repository.StoryRepository;
import snu.swpp.moment.ui.main_writeview.uistate.AiStoryState;
import snu.swpp.moment.ui.main_writeview.uistate.CompletionState;
import snu.swpp.moment.ui.main_writeview.uistate.CompletionStoreResultState;
import snu.swpp.moment.ui.main_writeview.uistate.MomentUiState;
import snu.swpp.moment.ui.main_writeview.uistate.StoryUiState;
import snu.swpp.moment.utils.EmotionMap;
import snu.swpp.moment.utils.TimeConverter;

public class TodayViewModel extends ViewModel {

    // 모먼트 작성
    private final MutableLiveData<MomentUiState> momentState = new MutableLiveData<>();

    // 하루 마무리
    private final MutableLiveData<CompletionState> completionState = new MutableLiveData<>();
    private final MutableLiveData<AiStoryState> aiStoryState = new MutableLiveData<>();
    private final MutableLiveData<CompletionStoreResultState> storyResultState = new MutableLiveData<>();
    private final MutableLiveData<CompletionStoreResultState> emotionResultState = new MutableLiveData<>();
    private final MutableLiveData<CompletionStoreResultState> tagsResultState = new MutableLiveData<>();
    private final MutableLiveData<CompletionStoreResultState> scoreResultState = new MutableLiveData<>();

    private final GetStoryUseCase getStoryUseCase;
    private final AuthenticationRepository authenticationRepository;
    private final MomentRepository momentRepository;
    private final StoryRepository storyRepository;

    private final int REFRESH_TOKEN_EXPIRED = 1;

    public TodayViewModel(
        AuthenticationRepository authenticationRepository,
        MomentRepository momentRepository,
        StoryRepository storyRepository
    ) {
        this.authenticationRepository = authenticationRepository;
        this.momentRepository = momentRepository;
        this.storyRepository = storyRepository;
        this.getStoryUseCase = new GetStoryUseCase(authenticationRepository, storyRepository);
    }

    public MomentUiState getMomentState() {
        return momentState.getValue();
    }

    public void getMoment(LocalDate localDate) {
        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        int date = localDate.getDayOfMonth();
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

    public void getAiStory() {
        authenticationRepository.isTokenValid(new WriteViewTokenCallback() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                storyRepository.getAIGeneratedStory(access_token, new AIStoryCallback() {
                    @Override
                    public void onSuccess(AIStoryGetResponse response) {
                        String title = response.getTitle();
                        String content = response.getStory();
                        aiStoryState.setValue(new AiStoryState(null, title, content));
                    }

                    @Override
                    public void onFailure(Exception error) {
                        aiStoryState.setValue(new AiStoryState(error, "", ""));
                    }
                });
            }
        });
    }

    public void getStory(LocalDate localDate) {
        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        int date = localDate.getDayOfMonth();
        getStoryUseCase.getStory(year, month, date);
    }

    public void notifyCompletion() {
        authenticationRepository.isTokenValid(new WriteViewTokenCallback() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();

                LocalDate today = TimeConverter.getToday();
                long[] todayTomorrowTimestamp = TimeConverter.getOneDayIntervalTimestamps(
                    today.getYear(), today.getMonthValue(), today.getDayOfMonth());
                long start = todayTomorrowTimestamp[0];
                long end = todayTomorrowTimestamp[1] + 1;

                storyRepository.notifyCompletion(access_token, start, end,
                    new StoryCompletionNotifyCallBack() {
                        @Override
                        public void onSuccess(StoryCompletionNotifyResponse response) {
                            completionState.setValue(new CompletionState(null, response.getId()));
                        }

                        @Override
                        public void onFailure(Exception error) {
                            completionState.setValue(new CompletionState(error, -1));
                        }
                    });
            }
        });
    }

    public void saveStory(String title, String content) {
        authenticationRepository.isTokenValid(new WriteViewTokenCallback() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                storyRepository.saveStory(access_token, title, content, new StorySaveCallback() {
                    @Override
                    public void onSuccess() {
                        storyResultState.setValue(new CompletionStoreResultState(null));
                    }

                    @Override
                    public void onFailure(Exception error) {
                        storyResultState.setValue(new CompletionStoreResultState(error));
                    }
                });
            }
        });
    }

    public void saveEmotion(int emotionInt) {
        authenticationRepository.isTokenValid(new WriteViewTokenCallback() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                String emotion = EmotionMap.getEmotion(emotionInt);
                storyRepository.saveEmotion(access_token, emotion, new EmotionSaveCallback() {
                    @Override
                    public void onSuccess() {
                        emotionResultState.setValue(new CompletionStoreResultState(null));
                    }

                    @Override
                    public void onFailure(Exception error) {
                        emotionResultState.setValue(new CompletionStoreResultState(error));
                    }
                });
            }
        });
    }

    public void saveScore(int score) {
        authenticationRepository.isTokenValid(new WriteViewTokenCallback() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                int story_id = getStoryUseCase.getStoryId();
                storyRepository.saveScore(access_token, story_id, score, new ScoreSaveCallback() {
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
        });
    }

    public void saveHashtags(String content) {
        authenticationRepository.isTokenValid(new WriteViewTokenCallback() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                int story_id = getStoryUseCase.getStoryId();
                storyRepository.saveHashtags(access_token, story_id, content,
                    new HashtagSaveCallback() {
                        @Override
                        public void onSuccess() {
                            tagsResultState.setValue(new CompletionStoreResultState(null));
                        }

                        @Override
                        public void onFailure(Exception error) {
                            tagsResultState.setValue(new CompletionStoreResultState(error));
                        }
                    });
            }
        });
    }
    public void observeMomentState(Observer<MomentUiState> observer) {
        momentState.observeForever(observer);
    }

    public void observeSavedStoryState(Observer<StoryUiState> observer) {
        getStoryUseCase.observeStoryState(observer);
    }

    public void observeCompletionState(Observer<CompletionState> observer) {
        completionState.observeForever(observer);
    }

    public void observeAiStoryState(Observer<AiStoryState> observer) {
        aiStoryState.observeForever(observer);
    }

    public void observeStoryResultState(Observer<CompletionStoreResultState> observer) {
        storyResultState.observeForever(observer);
    }

    public void observeEmotionResultState(Observer<CompletionStoreResultState> observer) {
        emotionResultState.observeForever(observer);
    }

    public void observeTagsResultState(Observer<CompletionStoreResultState> observer) {
        tagsResultState.observeForever(observer);
    }


    abstract class WriteViewTokenCallback implements TokenCallBack {

        @Override
        public void onFailure() {
            momentState.setValue(new MomentUiState(REFRESH_TOKEN_EXPIRED, null));
        }
    }
}