package snu.swpp.moment.ui.main_writeview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import snu.swpp.moment.data.callback.AiStoryCallback;
import snu.swpp.moment.data.callback.EmotionSaveCallback;
import snu.swpp.moment.data.callback.HashtagSaveCallback;
import snu.swpp.moment.data.callback.MomentGetCallBack;
import snu.swpp.moment.data.callback.MomentWriteCallBack;
import snu.swpp.moment.data.callback.ScoreSaveCallback;
import snu.swpp.moment.data.callback.StoryCompletionNotifyCallBack;
import snu.swpp.moment.data.callback.StoryGetCallBack;
import snu.swpp.moment.data.callback.StorySaveCallback;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.MomentPairModel;
import snu.swpp.moment.data.model.StoryModel;
import snu.swpp.moment.data.model.TokenModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;
import snu.swpp.moment.data.repository.StoryRepository;
import snu.swpp.moment.data.source.MomentRemoteDataSource;
import snu.swpp.moment.data.source.StoryRemoteDataSource;
import snu.swpp.moment.exception.InvalidEmotionException;
import snu.swpp.moment.exception.InvalidHashtagSaveRequestException;
import snu.swpp.moment.exception.InvalidScoreSaveRequestException;
import snu.swpp.moment.exception.InvalidStoryCompletionTimeException;
import snu.swpp.moment.exception.NoInternetException;
import snu.swpp.moment.exception.UnauthorizedAccessException;
import snu.swpp.moment.exception.UnknownErrorException;
import snu.swpp.moment.ui.main_writeview.uistate.AiStoryState;
import snu.swpp.moment.ui.main_writeview.uistate.CompletionState;
import snu.swpp.moment.ui.main_writeview.uistate.CompletionStoreResultState;
import snu.swpp.moment.ui.main_writeview.uistate.MomentUiState;
import snu.swpp.moment.ui.main_writeview.uistate.StoryUiState;
import snu.swpp.moment.ui.main_writeview.viewmodel.GetStoryUseCase;
import snu.swpp.moment.ui.main_writeview.viewmodel.SaveScoreUseCase;
import snu.swpp.moment.ui.main_writeview.viewmodel.TodayViewModel;

@RunWith(MockitoJUnitRunner.class)
public class TodayViewModelTest {

    private TodayViewModel todayViewModel;

    @Mock
    private MomentRemoteDataSource momentDataSource;
    @Mock
    private StoryRemoteDataSource storyDataSource;

    @Spy
    @InjectMocks
    private AuthenticationRepository authenticationRepository;
    @Spy
    @InjectMocks
    private MomentRepository momentRepository;
    @Spy
    @InjectMocks
    private StoryRepository storyRepository;

    private GetStoryUseCase getStoryUseCase;
    private SaveScoreUseCase saveScoreUseCase;

    // For testing with LiveData
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() {
        // 사용자 token 검사 로직 회피
        doAnswer(invocation -> {
            TokenCallBack callback = (TokenCallBack) invocation.getArguments()[0];
            callback.onSuccess();
            return null;
        }).when(authenticationRepository).isTokenValid(any());
        doReturn(new TokenModel("access", "refresh")).when(authenticationRepository).getToken();

        // usecase들은 annotation으로 정의했을 때 잘 안 됐음
        getStoryUseCase = spy(new GetStoryUseCase(authenticationRepository, storyRepository));
        saveScoreUseCase = spy(new SaveScoreUseCase(authenticationRepository, storyRepository));

        todayViewModel = new TodayViewModel(
            authenticationRepository,
            momentRepository,
            storyRepository,
            getStoryUseCase,
            saveScoreUseCase
        );
    }

    /* 모먼트 가져오기 성공 */
    @Test
    public void getMoment_success() {
        // Given
        final MomentPairModel momentPair = new MomentPairModel(
            1,
            "moment",
            "reply",
            0,
            0
        );
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            MomentGetCallBack callback = (MomentGetCallBack) args[3];
            callback.onSuccess(Arrays.asList(momentPair));
            return null;
        }).when(momentDataSource).getMoment(anyString(), anyLong(), anyLong(), any());

        Observer<MomentUiState> observer = momentUiState -> {
        };
        todayViewModel.observeMomentState(observer);

        // When
        todayViewModel.getMoment(LocalDateTime.now());

        // Then
        MomentUiState momentUiState = todayViewModel.getMomentState();
        MomentPairModel resultMomentPair = momentUiState.getMomentPairList().get(0);
        System.out.println(resultMomentPair.getMoment() + " " + resultMomentPair.getReply());
        System.out.println(momentUiState.getError());
        assertEquals(resultMomentPair, momentPair);
        assertNull(momentUiState.getError());
    }

    /* 모먼트 가져오기 실패 (authorization 에러) */
    @Test
    public void getMoment_fail() {
        // Given
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            MomentGetCallBack callback = (MomentGetCallBack) args[3];
            callback.onFailure(new UnauthorizedAccessException());
            return null;
        }).when(momentDataSource).getMoment(anyString(), anyLong(), anyLong(), any());

        Observer<MomentUiState> observer = momentUiState -> {
        };
        todayViewModel.observeMomentState(observer);

        // When
        todayViewModel.getMoment(LocalDateTime.now());

        // Then
        MomentUiState momentUiState = todayViewModel.getMomentState();
        System.out.println(momentUiState.getError());
        assertEquals(momentUiState.getMomentPairList().size(), 0);
        assertTrue(momentUiState.getError() instanceof UnauthorizedAccessException);
    }

    /* 새 모먼트 작성 성공 */
    @Test
    public void writeMoment_success() {
        // Given
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            String moment = (String) args[1];
            MomentWriteCallBack callback = (MomentWriteCallBack) args[2];

            MomentPairModel momentPair = new MomentPairModel(
                1, moment, "new reply", 0, 0
            );
            callback.onSuccess(momentPair);
            return null;
        }).when(momentDataSource).writeMoment(anyString(), anyString(), any());

        Observer<MomentUiState> observer = momentUiState -> {
        };
        todayViewModel.observeMomentState(observer);

        // When
        final String newMoment = "new moment";
        todayViewModel.writeMoment(newMoment);

        // Then
        MomentUiState momentUiState = todayViewModel.getMomentState();
        MomentPairModel resultMomentPair = momentUiState.getMomentPairList().get(0);
        System.out.println(resultMomentPair.getMoment() + " " + resultMomentPair.getReply());
        System.out.println(momentUiState.getError());
        assertEquals(resultMomentPair.getMoment(), newMoment);
        assertNull(momentUiState.getError());
    }

    /* 새 모먼트 작성 실패 (인터넷 연결 에러) */
    @Test
    public void writeMoment_fail() {
        // Given
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            MomentWriteCallBack callback = (MomentWriteCallBack) args[2];

            callback.onFailure(new NoInternetException());
            return null;
        }).when(momentDataSource).writeMoment(anyString(), anyString(), any());

        Observer<MomentUiState> observer = momentUiState -> {
        };
        todayViewModel.observeMomentState(observer);

        // When
        final String newMoment = "new moment";
        todayViewModel.writeMoment(newMoment);

        // Then
        MomentUiState momentUiState = todayViewModel.getMomentState();
        System.out.println(momentUiState.getError());
        assertEquals(momentUiState.getMomentPairList().size(), 0);
        assertTrue(momentUiState.getError() instanceof NoInternetException);
    }

    @Test
    public void getAiStory_success() {
        // Given
        doAnswer(invocation -> {
            AiStoryCallback callback = (AiStoryCallback) invocation.getArguments()[1];
            callback.onSuccess("title", "content");
            return null;
        }).when(storyDataSource).getAiGeneratedStory(anyString(), any());

        Observer<AiStoryState> observer = aiStoryState -> {
            // Then
            System.out.println(aiStoryState.getContent() + " " + aiStoryState.getTitle());
            assertNull(aiStoryState.getError());
            assertEquals(aiStoryState.getTitle(), "title");
            assertEquals(aiStoryState.getContent(), "content");
        };
        todayViewModel.observeAiStoryState(observer);

        // When
        todayViewModel.getAiStory();
    }

    @Test
    public void getAiStory_fail() {
        // Given
        doAnswer(invocation -> {
            AiStoryCallback callback = (AiStoryCallback) invocation.getArguments()[1];
            callback.onFailure(new NoInternetException());
            return null;
        }).when(storyDataSource).getAiGeneratedStory(anyString(), any());

        Observer<AiStoryState> observer = aiStoryState -> {
            // Then
            System.out.println(aiStoryState.getContent() + " " + aiStoryState.getTitle());
            assertTrue(aiStoryState.getError() instanceof NoInternetException);
            assertEquals(aiStoryState.getTitle(), "");
            assertEquals(aiStoryState.getContent(), "");
        };
        todayViewModel.observeAiStoryState(observer);

        // When
        todayViewModel.getAiStory();
    }

    @Test
    public void getStory_success() {
        // Given
        final StoryModel story = new StoryModel(
            1,
            "excited1",
            0,
            "title",
            "content",
            new ArrayList<>(),
            0L,
            false,
            false
        );
        doAnswer(invocation -> {
            StoryGetCallBack callback = (StoryGetCallBack) invocation.getArguments()[3];
            callback.onSuccess(Arrays.asList(story));
            return null;
        }).when(storyDataSource).getStory(anyString(), anyLong(), anyLong(), any());

        Observer<StoryUiState> observer = storyUiState -> {
            // Then
            System.out.println(storyUiState.getContent() + " " + storyUiState.getTitle());
            assertNull(storyUiState.getError());
            assertFalse(storyUiState.isEmpty());
            assertEquals(storyUiState.getTitle(), "title");
            assertEquals(storyUiState.getContent(), "content");
            assertEquals(storyUiState.getEmotion(), 0);
            assertEquals(storyUiState.getTags().size(), 0);
            assertFalse(storyUiState.isPointCompleted());
        };
        todayViewModel.observeSavedStoryState(observer);

        // When
        todayViewModel.getStory(LocalDateTime.now());
    }

    @Test
    public void getStory_fail() {
        // Given
        doAnswer(invocation -> {
            StoryGetCallBack callback = (StoryGetCallBack) invocation.getArguments()[3];
            callback.onFailure(new UnknownErrorException());
            return null;
        }).when(storyDataSource).getStory(anyString(), anyLong(), anyLong(), any());

        Observer<StoryUiState> observer = storyUiState -> {
            // Then
            System.out.println(storyUiState.getError());
            assertTrue(storyUiState.getError() instanceof UnknownErrorException);
            assertTrue(storyUiState.isEmpty());
            assertEquals(storyUiState.getTitle(), "");
            assertEquals(storyUiState.getContent(), "");
            assertEquals(storyUiState.getEmotion(), 0);
            assertEquals(storyUiState.getTags().size(), 0);
            assertFalse(storyUiState.isPointCompleted());
        };
        todayViewModel.observeSavedStoryState(observer);

        // When
        todayViewModel.getStory(LocalDateTime.now());
    }

    @Test
    public void notifyCompletion_success() {
        // Given
        final int storyId = 111;
        doAnswer(invocation -> {
            StoryCompletionNotifyCallBack callback = (StoryCompletionNotifyCallBack) invocation.getArguments()[3];
            callback.onSuccess(storyId);
            return null;
        }).when(storyDataSource).notifyCompletion(anyString(), anyLong(), anyLong(), any());

        Observer<CompletionState> observer = completionState -> {
            // Then
            System.out.println(completionState.getStoryId());
            assertNull(completionState.getError());
            assertEquals(completionState.getStoryId(), storyId);
        };
        todayViewModel.observeCompletionState(observer);

        // When
        todayViewModel.notifyCompletion();
    }

    @Test
    public void notifyCompletion_fail() {
        // Given
        doAnswer(invocation -> {
            StoryCompletionNotifyCallBack callback = (StoryCompletionNotifyCallBack) invocation.getArguments()[3];
            callback.onFailure(new InvalidStoryCompletionTimeException());
            return null;
        }).when(storyDataSource).notifyCompletion(anyString(), anyLong(), anyLong(), any());

        Observer<CompletionState> observer = completionState -> {
            // Then
            System.out.println(completionState.getStoryId());
            assertTrue(completionState.getError() instanceof InvalidStoryCompletionTimeException);
            assertEquals(completionState.getStoryId(), -1);
        };
        todayViewModel.observeCompletionState(observer);

        // When
        todayViewModel.notifyCompletion();
    }

    @Test
    public void saveStory_success() {
        // Given
        doAnswer(invocation -> {
            StorySaveCallback callback = (StorySaveCallback) invocation.getArguments()[3];
            callback.onSuccess();
            return null;
        }).when(storyDataSource).saveStory(anyString(), anyString(), anyString(), any());

        Observer<CompletionStoreResultState> observer = storeResultState -> {
            // Then
            assertNull(storeResultState.getError());
        };
        todayViewModel.observeStoryResultState(observer);

        // When
        todayViewModel.saveStory("title", "content");
    }

    @Test
    public void saveStory_fail() {
        // Given
        doAnswer(invocation -> {
            StorySaveCallback callback = (StorySaveCallback) invocation.getArguments()[3];
            callback.onFailure(new UnauthorizedAccessException());
            return null;
        }).when(storyDataSource).saveStory(anyString(), anyString(), anyString(), any());

        Observer<CompletionStoreResultState> observer = storeResultState -> {
            // Then
            assertTrue(storeResultState.getError() instanceof UnauthorizedAccessException);
        };
        todayViewModel.observeStoryResultState(observer);

        // When
        todayViewModel.saveStory("title", "content");
    }

    @Test
    public void saveEmotion_success() {
        // Given
        doAnswer(invocation -> {
            EmotionSaveCallback callback = (EmotionSaveCallback) invocation.getArguments()[2];
            callback.onSuccess();
            return null;
        }).when(storyDataSource).saveEmotion(anyString(), anyString(), any());

        Observer<CompletionStoreResultState> observer = storeResultState -> {
            // Then
            assertNull(storeResultState.getError());
        };
        todayViewModel.observeEmotionResultState(observer);

        // When
        todayViewModel.saveEmotion(0);
    }

    @Test
    public void saveEmotion_fail() {
        // Given
        doAnswer(invocation -> {
            EmotionSaveCallback callback = (EmotionSaveCallback) invocation.getArguments()[2];
            callback.onFailure(new InvalidEmotionException());
            return null;
        }).when(storyDataSource).saveEmotion(anyString(), anyString(), any());

        Observer<CompletionStoreResultState> observer = storeResultState -> {
            // Then
            assertTrue(storeResultState.getError() instanceof InvalidEmotionException);
        };
        todayViewModel.observeEmotionResultState(observer);

        // When
        todayViewModel.saveEmotion(0);
    }

    @Test
    public void saveHashtags_success() {
        // Given
        doAnswer(invocation -> {
            HashtagSaveCallback callback = (HashtagSaveCallback) invocation.getArguments()[3];
            callback.onSuccess();
            return null;
        }).when(storyDataSource).saveHashtags(anyString(), anyInt(), anyString(), any());
        doReturn(111).when(getStoryUseCase).getStoryId();

        Observer<CompletionStoreResultState> observer = storeResultState -> {
            // Then
            System.out.println("observer for saveHashtags");
            assertNull(storeResultState.getError());
        };
        todayViewModel.observeTagsResultState(observer);

        // When
        todayViewModel.saveHashtags("#tag");
    }

    @Test
    public void saveHashtags_fail() {
        // Given
        doAnswer(invocation -> {
            HashtagSaveCallback callback = (HashtagSaveCallback) invocation.getArguments()[3];
            callback.onFailure(new InvalidHashtagSaveRequestException());
            return null;
        }).when(storyDataSource).saveHashtags(anyString(), anyInt(), anyString(), any());
        doReturn(111).when(getStoryUseCase).getStoryId();

        Observer<CompletionStoreResultState> observer = storeResultState -> {
            // Then
            System.out.println("observer for saveHashtags");
            assertTrue(storeResultState.getError() instanceof InvalidHashtagSaveRequestException);
        };
        todayViewModel.observeTagsResultState(observer);

        // When
        todayViewModel.saveHashtags("#tag");
    }

    @Test
    public void saveScore_success() {
        // Given
        doAnswer(invocation -> {
            ScoreSaveCallback callback = (ScoreSaveCallback) invocation.getArguments()[3];
            callback.onSuccess();
            return null;
        }).when(storyDataSource).saveScore(anyString(), anyInt(), anyInt(), any());
        doReturn(111).when(getStoryUseCase).getStoryId();

        Observer<CompletionStoreResultState> observer = storeResultState -> {
            // Then
            System.out.println("observer for saveScore");
            assertNull(storeResultState.getError());
        };
        todayViewModel.observeScoreResultState(observer);

        // When
        todayViewModel.saveScore(3);
    }

    @Test
    public void saveScore_fail() {
        // Given
        doAnswer(invocation -> {
            ScoreSaveCallback callback = (ScoreSaveCallback) invocation.getArguments()[3];
            callback.onFailure(new InvalidScoreSaveRequestException());
            return null;
        }).when(storyDataSource).saveScore(anyString(), anyInt(), anyInt(), any());
        doReturn(111).when(getStoryUseCase).getStoryId();

        Observer<CompletionStoreResultState> observer = storeResultState -> {
            // Then
            System.out.println("observer for saveScore");
            assertTrue(storeResultState.getError() instanceof InvalidScoreSaveRequestException);
        };
        todayViewModel.observeScoreResultState(observer);

        // When
        todayViewModel.saveScore(3);
    }
}