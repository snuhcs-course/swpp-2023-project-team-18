package snu.swpp.moment.ui.main_writeview;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
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
import snu.swpp.moment.data.callback.StoryGetCallBack;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.StoryModel;
import snu.swpp.moment.data.model.TokenModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.StoryRepository;
import snu.swpp.moment.data.source.StoryRemoteDataSource;
import snu.swpp.moment.data.source.UserLocalDataSource;
import snu.swpp.moment.exception.UnknownErrorException;
import snu.swpp.moment.ui.main_writeview.uistate.StoryUiState;

@RunWith(MockitoJUnitRunner.class)
public class GetStoryUseCaseTest {

    private GetStoryUseCase useCase;
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
        useCase = new GetStoryUseCase(authenticationRepository, storyRepository);
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
        }).when(dataSource).getStory(anyString(), anyLong(), anyLong(), any());

        Observer<StoryUiState> observer = storyUiState -> {
            // Then
            System.out.println("observer for success");
            assertNull(storyUiState.getError());
            assertFalse(storyUiState.isEmpty());
            assertEquals(storyUiState.getTitle(), "title");
            assertEquals(storyUiState.getContent(), "content");
            assertEquals(storyUiState.getEmotion(), 0);
            assertEquals(storyUiState.getTags().size(), 0);
            assertFalse(storyUiState.isPointCompleted());
        };
        useCase.observeStoryState(observer);

        // When
        useCase.getStory(LocalDateTime.now());
    }

    @Test
    public void getStory_fail() {
        doAnswer(invocation -> {
            StoryGetCallBack callback = (StoryGetCallBack) invocation.getArguments()[3];
            callback.onFailure(new UnknownErrorException());
            return null;
        }).when(dataSource).getStory(anyString(), anyLong(), anyLong(), any());

        Observer<StoryUiState> observer = storyUiState -> {
            // Then
            System.out.println("observer for failure");
            assertTrue(storyUiState.getError() instanceof UnknownErrorException);
            assertTrue(storyUiState.isEmpty());
            assertEquals(storyUiState.getTitle(), "");
            assertEquals(storyUiState.getContent(), "");
            assertEquals(storyUiState.getEmotion(), 0);
            assertEquals(storyUiState.getTags().size(), 0);
            assertFalse(storyUiState.isPointCompleted());
        };
        useCase.observeStoryState(observer);

        // When
        useCase.getStory(LocalDateTime.now());
    }
}