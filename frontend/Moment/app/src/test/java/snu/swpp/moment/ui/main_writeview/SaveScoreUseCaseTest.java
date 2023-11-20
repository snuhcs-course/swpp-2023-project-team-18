package snu.swpp.moment.ui.main_writeview;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import snu.swpp.moment.data.callback.ScoreSaveCallback;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.TokenModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.StoryRepository;
import snu.swpp.moment.data.source.StoryRemoteDataSource;
import snu.swpp.moment.exception.InvalidScoreSaveRequestException;
import snu.swpp.moment.ui.main_writeview.uistate.CompletionStoreResultState;
import snu.swpp.moment.ui.main_writeview.viewmodel.SaveScoreUseCase;

@RunWith(MockitoJUnitRunner.class)
public class SaveScoreUseCaseTest {

    private SaveScoreUseCase useCase;
    @Mock
    private StoryRemoteDataSource dataSource;

    @Spy
    @InjectMocks
    private AuthenticationRepository authenticationRepository;

    @Spy
    @InjectMocks
    private StoryRepository storyRepository;

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
        useCase = new SaveScoreUseCase(authenticationRepository, storyRepository);
    }

    @Test
    public void saveScore_success() {
        // Given
        doAnswer(invocation -> {
            ScoreSaveCallback callback = (ScoreSaveCallback) invocation.getArguments()[3];
            callback.onSuccess();
            return null;
        }).when(dataSource).saveScore(anyString(), anyInt(), anyInt(), any());

        Observer<CompletionStoreResultState> observer = storyResultState -> {
            // Then
            assertNull(storyResultState.getError());
        };
        useCase.observeScoreResultState(observer);

        // When
        int story_id = 1;
        int score = 2;
        useCase.saveScore(story_id, score);
    }

    @Test
    public void saveScore_fail() {
        // Given
        doAnswer(invocation -> {
            ScoreSaveCallback callback = (ScoreSaveCallback) invocation.getArguments()[3];
            callback.onFailure(new InvalidScoreSaveRequestException());
            return null;
        }).when(dataSource).saveScore(anyString(), anyInt(), anyInt(), any());

        Observer<CompletionStoreResultState> observer = storeResultState -> {
            // Then
            assertTrue(storeResultState.getError() instanceof InvalidScoreSaveRequestException);
        };
        useCase.observeScoreResultState(observer);

        // When
        int story_id = 1;
        int score = 2;
        useCase.saveScore(story_id, score);
    }
}