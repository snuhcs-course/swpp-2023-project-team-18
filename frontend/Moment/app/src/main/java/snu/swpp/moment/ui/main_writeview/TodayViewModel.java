package snu.swpp.moment.ui.main_writeview;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import snu.swpp.moment.data.callback.MomentGetCallBack;
import snu.swpp.moment.data.callback.MomentWriteCallBack;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.MomentPairModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;
import snu.swpp.moment.data.repository.StoryRepository;
import snu.swpp.moment.exception.UnauthorizedAccessException;
import snu.swpp.moment.ui.main_writeview.uistate.AiStoryState;
import snu.swpp.moment.ui.main_writeview.uistate.CompletionState;
import snu.swpp.moment.ui.main_writeview.uistate.CompletionStoreResultState;
import snu.swpp.moment.ui.main_writeview.uistate.MomentUiState;
import snu.swpp.moment.ui.main_writeview.uistate.StoryUiState;
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
                        public void onSuccess(List<MomentPairModel> momentPairList) {
                            momentState.setValue(
                                new MomentUiState(null, momentPairList)
                            );
                        }

                        @Override
                        public void onFailure(Exception error) {
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
                            List<MomentPairModel> momentPairList = momentState.getValue()
                                .getMomentPairList();
                            momentPairList.add(momentPair);
                            momentState.setValue(new MomentUiState(null, momentPairList));
                        }

                        @Override
                        public void onFailure(Exception error) {
                            momentState.setValue(
                                new MomentUiState(error, null)
                            );
                        }
                    });
            }
        });
    }

    public void getAiStory() {
        // TODO: AI 요약 받아오는 API 구현
    }

    public void getStory(LocalDate localDate) {
        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        int date = localDate.getDayOfMonth();
        getStoryUseCase.getStory(year, month, date);
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
        // TODO: 점수 저장 후에 뭘 해야 하지?
        scoreResultState.observeForever(observer);
    }


    abstract class WriteViewTokenCallback implements TokenCallBack {

        @Override
        public void onFailure() {
            momentState.setValue(new MomentUiState(new UnauthorizedAccessException(), null));
        }
    }
}