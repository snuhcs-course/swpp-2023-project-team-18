package snu.swpp.moment.ui.main_writeview.viewmodel;

import android.util.Log;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import snu.swpp.moment.data.callback.AiStoryCallback;
import snu.swpp.moment.data.callback.EmotionSaveCallback;
import snu.swpp.moment.data.callback.HashtagSaveCallback;
import snu.swpp.moment.data.callback.MomentGetCallBack;
import snu.swpp.moment.data.callback.MomentWriteCallBack;
import snu.swpp.moment.data.callback.NudgeGetCallback;
import snu.swpp.moment.data.callback.NudgeDeleteCallback;
import snu.swpp.moment.data.callback.StoryCompletionNotifyCallBack;
import snu.swpp.moment.data.callback.StorySaveCallback;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.MomentPairModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;
import snu.swpp.moment.data.repository.NudgeRepository;
import snu.swpp.moment.data.repository.StoryRepository;
import snu.swpp.moment.exception.UnauthorizedAccessException;
import snu.swpp.moment.ui.main_writeview.uistate.AiStoryState;
import snu.swpp.moment.ui.main_writeview.uistate.CompletionState;
import snu.swpp.moment.ui.main_writeview.uistate.CompletionStoreResultState;
import snu.swpp.moment.ui.main_writeview.uistate.MomentUiState;
import snu.swpp.moment.ui.main_writeview.uistate.NudgeUiState;
import snu.swpp.moment.ui.main_writeview.uistate.StoryUiState;
import snu.swpp.moment.utils.EmotionMap;
import snu.swpp.moment.utils.TimeConverter;

public class TodayViewModel extends ViewModel {

    // 모먼트 작성
    private final MutableLiveData<MomentUiState> momentState = new MutableLiveData<>(
        new MomentUiState(null, new ArrayList<>()));

    // 하루 마무리
    private final MutableLiveData<CompletionState> completionState = new MutableLiveData<>();
    private final MutableLiveData<AiStoryState> aiStoryState = new MutableLiveData<>();
    private final MutableLiveData<CompletionStoreResultState> storyResultState = new MutableLiveData<>();
    private final MutableLiveData<CompletionStoreResultState> emotionResultState = new MutableLiveData<>();
    private final MutableLiveData<CompletionStoreResultState> tagsResultState = new MutableLiveData<>();
    private final MutableLiveData<NudgeUiState> nudgeState = new MutableLiveData<>();
    private final SaveScoreUseCase saveScoreUseCase;

    private final GetStoryUseCase getStoryUseCase;
    private final AuthenticationRepository authenticationRepository;
    private final MomentRepository momentRepository;
    private final StoryRepository storyRepository;
    private final NudgeRepository nudgeRepository;

    public TodayViewModel(
        AuthenticationRepository authenticationRepository,
        MomentRepository momentRepository,
        StoryRepository storyRepository,
        NudgeRepository nudgeRepository,
        GetStoryUseCase getStoryUseCase,
        SaveScoreUseCase saveScoreUseCase
    ) {
        this.authenticationRepository = authenticationRepository;
        this.momentRepository = momentRepository;
        this.storyRepository = storyRepository;
        this.nudgeRepository = nudgeRepository;
        this.getStoryUseCase = getStoryUseCase;
        this.saveScoreUseCase = saveScoreUseCase;
    }

    public MomentUiState getMomentState() {
        return momentState.getValue();
    }

    public void getMoment(LocalDateTime now) {
        long[] dayInterval = TimeConverter.getOneDayIntervalTimestamps(now);

        authenticationRepository.isTokenValid(new TodayViewModel.WriteViewTokenCallback() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                momentRepository.getMoment(access_token, dayInterval[0], dayInterval[1],
                    new MomentGetCallBack() {
                        @Override
                        public void onSuccess(List<MomentPairModel> momentPairList) {
                            momentState.setValue(
                                new MomentUiState(null, momentPairList)
                            );
                        }

                        @Override
                        public void onFailure(Exception error) {
                            momentState.setValue(MomentUiState.withError(error));
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
                            List<MomentPairModel> momentPairList = momentState.getValue()
                                .getMomentPairList();
                            momentPairList.add(momentPair);
                            momentState.setValue(new MomentUiState(null, momentPairList));
                        }

                        @Override
                        public void onFailure(Exception error) {
                            momentState.setValue(MomentUiState.withError(error));
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
                storyRepository.getAIGeneratedStory(access_token, new AiStoryCallback() {
                    @Override
                    public void onSuccess(String title, String content) {
                        aiStoryState.setValue(new AiStoryState(null, title, content));
                    }

                    @Override
                    public void onFailure(Exception error) {
                        aiStoryState.setValue(AiStoryState.withError(error));
                    }
                });
            }
        });
    }

    public void getStory(LocalDateTime now) {
        getStoryUseCase.getStory(now);
    }

    public void notifyCompletion() {
        Log.d("TodayViewModel", "notifyCompletion: called");
        authenticationRepository.isTokenValid(new WriteViewTokenCallback() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();

                long[] todayTomorrowTimestamp = TimeConverter.getOneDayIntervalTimestamps(
                    LocalDateTime.now());
                long start = todayTomorrowTimestamp[0];
                long end = todayTomorrowTimestamp[1] + 1;

                storyRepository.notifyCompletion(access_token, start, end,
                    new StoryCompletionNotifyCallBack() {
                        @Override
                        public void onSuccess(int storyId) {
                            completionState.setValue(new CompletionState(null, storyId));
                            Log.d("setStoryId", String.valueOf(storyId));
                            getStoryUseCase.setStoryId(storyId);
                        }

                        @Override
                        public void onFailure(Exception error) {
                            completionState.setValue(CompletionState.withError(error));
                        }
                    });
            }
        });
    }

    public void saveStory(String title, String content) {
        Log.d("TodayViewModel", "saveStory: called");
        final String titleToSave = (title.isEmpty()) ? "제목 없음" : title;
        authenticationRepository.isTokenValid(new WriteViewTokenCallback() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                storyRepository.saveStory(access_token, titleToSave, content,
                    new StorySaveCallback() {
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
                String emotion = EmotionMap.getEmotionString(emotionInt);
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

    public void saveHashtags(String content) {
        authenticationRepository.isTokenValid(new WriteViewTokenCallback() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                int storyId = getStoryUseCase.getStoryId();
                if (storyId == -1) {
                    Log.d("TodayViewModel", "saveHashtags: StoryId is not set yet (id=-1)");
                }

                storyRepository.saveHashtags(access_token, storyId, content,
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

    public void saveScore(int score) {
        int storyId = getStoryUseCase.getStoryId();
        if (storyId == -1) {
            Log.d("TodayViewModel", "saveScore: StoryId is not set yet (id=-1)");
            return;
        }
        saveScoreUseCase.saveScore(storyId, score);
    }

    public void getNudge(LocalDateTime now) {

        authenticationRepository.isTokenValid(new WriteViewTokenCallback() {
            long[] todayTomorrowTimestamp = TimeConverter.getOneDayIntervalTimestamps(now);
            long start = todayTomorrowTimestamp[0];
            long end = todayTomorrowTimestamp[1] + 1;

            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                nudgeRepository.getNudge(access_token, start, end, new NudgeGetCallback() {
                    @Override
                    public void onSuccess(String nudge) {
                        nudgeState.setValue(new NudgeUiState(null, nudge.isEmpty(), nudge));
                    }

                    @Override
                    public void onFailure(Exception error) {
                        nudgeState.setValue(NudgeUiState.withError(error));
                    }
                });
            }
        });


    }

    public void deleteNudge() {
        authenticationRepository.isTokenValid(new WriteViewTokenCallback() {

            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                nudgeRepository.deleteNudge(access_token, new NudgeDeleteCallback() {
                    @Override
                    public void onSuccess() {
                        nudgeState.setValue(new NudgeUiState(null, true, ""));
                    }

                    @Override
                    public void onFailure(Exception error) {
                        nudgeState.setValue(NudgeUiState.withError(error));
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

    public void observeScoreResultState(Observer<CompletionStoreResultState> observer) {
        saveScoreUseCase.observeScoreResultState(observer);
    }

    public void observeNudgeState(Observer<NudgeUiState> observer) {
        nudgeState.observeForever(observer);
    }


    abstract class WriteViewTokenCallback implements TokenCallBack {

        @Override
        public void onFailure() {
            momentState.setValue(MomentUiState.withError(new UnauthorizedAccessException()));
        }
    }
}