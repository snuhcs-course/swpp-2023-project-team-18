package snu.swpp.moment.ui.main_writeview;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;
import java.time.LocalDate;
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
import snu.swpp.moment.data.callback.MomentGetCallBack;
import snu.swpp.moment.data.callback.ScoreSaveCallback;
import snu.swpp.moment.data.callback.StoryGetCallBack;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.MomentPairModel;
import snu.swpp.moment.data.model.StoryModel;
import snu.swpp.moment.data.model.TokenModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;
import snu.swpp.moment.data.repository.StoryRepository;
import snu.swpp.moment.data.source.MomentRemoteDataSource;
import snu.swpp.moment.data.source.StoryRemoteDataSource;
import snu.swpp.moment.exception.InvalidScoreSaveRequestException;
import snu.swpp.moment.exception.UnauthorizedAccessException;
import snu.swpp.moment.exception.UnknownErrorException;
import snu.swpp.moment.ui.main_writeview.uistate.CompletionStoreResultState;
import snu.swpp.moment.ui.main_writeview.uistate.MomentUiState;
import snu.swpp.moment.ui.main_writeview.uistate.StoryUiState;
import snu.swpp.moment.ui.main_writeview.viewmodel.DailyViewModel;
import snu.swpp.moment.ui.main_writeview.viewmodel.GetStoryUseCase;
import snu.swpp.moment.ui.main_writeview.viewmodel.SaveScoreUseCase;
import snu.swpp.moment.utils.TimeConverter;

@RunWith(MockitoJUnitRunner.class)
public class DailyViewModelTest {

    private DailyViewModel viewModel;
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
    private final int minusDays = 2;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() {
        doAnswer(invocation -> {
            TokenCallBack callback = (TokenCallBack) invocation.getArguments()[0];
            callback.onSuccess();
            return null;
        }).when(authenticationRepository).isTokenValid(any());
        doReturn(new TokenModel("access", "refresh"))
            .when(authenticationRepository).getToken();
        getStoryUseCase = spy(new GetStoryUseCase(authenticationRepository, storyRepository));
        saveScoreUseCase = spy(new SaveScoreUseCase(authenticationRepository, storyRepository));
        viewModel = new DailyViewModel(
            authenticationRepository,
            momentRepository,
            getStoryUseCase,
            saveScoreUseCase
        );
    }


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
            // Then
            System.out.println("observer for getMoment success");
            assertNull(momentUiState.getError());
            assertEquals(momentUiState.getMomentPairList().get(0), momentPair);
        };
        viewModel.observeMomentState(observer);

        // When
        LocalDate date = TimeConverter.getToday().minusDays(minusDays);
        LocalDateTime dateTime = date.atTime(3, 0);
        viewModel.getMoment(dateTime);
    }

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
            // Then
            System.out.println("observer for getMoment failure");
            assertTrue(momentUiState.getError() instanceof UnauthorizedAccessException);
            assertEquals(momentUiState.getMomentPairList().size(), 0);
        };
        viewModel.observeMomentState(observer);

        // When
        LocalDate date = TimeConverter.getToday().minusDays(minusDays);
        LocalDateTime dateTime = date.atTime(3, 0);
        viewModel.getMoment(dateTime);
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
            false
        );
        doAnswer(invocation -> {
            StoryGetCallBack callback = (StoryGetCallBack) invocation.getArguments()[3];
            callback.onSuccess(Arrays.asList(story));
            return null;
        }).when(storyDataSource).getStory(anyString(), anyLong(), anyLong(), any());

        Observer<StoryUiState> observer = storyUiState -> {
            // Then
            System.out.println("observer for getStory success");
            assertNull(storyUiState.getError());
            assertFalse(storyUiState.isEmpty());
            assertEquals(storyUiState.getTitle(), "title");
            assertEquals(storyUiState.getContent(), "content");
            assertEquals(storyUiState.getEmotion(), 0);
            assertEquals(storyUiState.getTags().size(), 0);
            assertFalse(storyUiState.isPointCompleted());
        };
        viewModel.observeStoryState(observer);

        // When
        LocalDate date = TimeConverter.getToday().minusDays(minusDays);
        LocalDateTime dateTime = date.atTime(3, 0);
        viewModel.getStory(dateTime);
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
            System.out.println("observer for getStory failure");
            assertTrue(storyUiState.getError() instanceof UnknownErrorException);
            assertTrue(storyUiState.isEmpty());
            assertEquals(storyUiState.getTitle(), "");
            assertEquals(storyUiState.getContent(), "");
            assertEquals(storyUiState.getEmotion(), 0);
            assertEquals(storyUiState.getTags().size(), 0);
            assertFalse(storyUiState.isPointCompleted());
        };
        viewModel.observeStoryState(observer);

        // When
        LocalDate date = TimeConverter.getToday().minusDays(minusDays);
        LocalDateTime dateTime = date.atTime(3, 0);
        viewModel.getStory(dateTime);
    }

    @Test
    public void saveScore_success() {
        // Given
        doAnswer(invocation -> {
            ScoreSaveCallback callback = (ScoreSaveCallback) invocation.getArguments()[3];
            callback.onSuccess();
            return null;
        }).when(storyDataSource).saveScore(anyString(), anyInt(), anyInt(), any());
        doReturn(1).when(getStoryUseCase).getStoryId();

        Observer<CompletionStoreResultState> observer = storeResultState -> {
            // Then
            System.out.println("observer for saveScore success");
            assertNull(storeResultState.getError());
        };
        viewModel.observeScoreResultState(observer);

        // When
        viewModel.saveScore(2);
    }

    @Test
    public void saveScore_fail() {
        // Given
        doAnswer(invocation -> {
            ScoreSaveCallback callback = (ScoreSaveCallback) invocation.getArguments()[3];
            callback.onFailure(new InvalidScoreSaveRequestException());
            return null;
        }).when(storyDataSource).saveScore(anyString(), anyInt(), anyInt(), any());
        doReturn(1).when(getStoryUseCase).getStoryId();

        Observer<CompletionStoreResultState> observer = storeResultState -> {
            // Then
            System.out.println("observer for saveScore failure");
            assertTrue(storeResultState.getError() instanceof InvalidScoreSaveRequestException);
        };
        viewModel.observeScoreResultState(observer);

        // When
        viewModel.saveScore(2);
    }
}
