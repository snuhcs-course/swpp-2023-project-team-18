package snu.swpp.moment.ui.main_writeview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;
import java.time.LocalDateTime;
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
import snu.swpp.moment.data.callback.MomentWriteCallBack;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.MomentPairModel;
import snu.swpp.moment.data.model.TokenModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;
import snu.swpp.moment.data.repository.StoryRepository;
import snu.swpp.moment.data.source.MomentRemoteDataSource;
import snu.swpp.moment.exception.NoInternetException;
import snu.swpp.moment.exception.UnauthorizedAccessException;
import snu.swpp.moment.ui.main_writeview.uistate.MomentUiState;

@RunWith(MockitoJUnitRunner.class)
public class TodayViewModelTest {

    private TodayViewModel todayViewModel;

    @Mock
    private MomentRemoteDataSource momentDataSource;

    @Spy
    @InjectMocks
    private AuthenticationRepository authenticationRepository;
    @Spy
    @InjectMocks
    private MomentRepository momentRepository;
    @Spy
    @InjectMocks
    private StoryRepository storyRepository;
    @Spy
    @InjectMocks
    private GetStoryUseCase getStoryUseCase;
    @Spy
    @InjectMocks
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
            String moment = (String) args[1];
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
    public void getAiStory() {
    }

    @Test
    public void getStory() {
    }

    @Test
    public void notifyCompletion() {
    }

    @Test
    public void saveStory() {
    }

    @Test
    public void saveEmotion() {
    }

    @Test
    public void saveHashtags() {
    }

    @Test
    public void saveScore() {
    }

    @Test
    public void observeMomentState() {
    }

    @Test
    public void observeSavedStoryState() {
    }

    @Test
    public void observeCompletionState() {
    }

    @Test
    public void observeAiStoryState() {
    }

    @Test
    public void observeStoryResultState() {
    }

    @Test
    public void observeEmotionResultState() {
    }

    @Test
    public void observeTagsResultState() {
    }
}