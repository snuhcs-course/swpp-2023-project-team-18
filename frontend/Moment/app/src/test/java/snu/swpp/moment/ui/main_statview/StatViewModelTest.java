package snu.swpp.moment.ui.main_statview;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import junit.framework.TestCase;
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
import snu.swpp.moment.data.model.HashtagModel;
import snu.swpp.moment.data.model.StoryModel;
import snu.swpp.moment.data.model.TokenModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.StoryRepository;
import snu.swpp.moment.data.source.StoryRemoteDataSource;

@RunWith(MockitoJUnitRunner.class)
public class StatViewModelTest extends TestCase {

    private StatViewModel viewModel;

    @Mock
    private StoryRemoteDataSource storyDataSource;

    @Spy
    @InjectMocks
    private AuthenticationRepository authenticationRepository;


    @Spy
    @InjectMocks
    private StoryRepository storyRepository;

    @Before
    public void setUp() {
        doAnswer(invocation -> {
            TokenCallBack callback = (TokenCallBack) invocation.getArguments()[0];
            callback.onSuccess();
            return null;
        }).when(authenticationRepository).isTokenValid(any());
        doReturn(new TokenModel("access", "refresh"))
            .when(authenticationRepository).getToken();

        viewModel = new StatViewModel(authenticationRepository, storyRepository);
    }

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    static final LocalDate testDay = LocalDate.of(2023, 11, 3);
    static final List<StoryModel> stories = Arrays.asList(new StoryModel(
        1,
        "excited1",
        3,
        "title",
        "content",
        Arrays.asList(new HashtagModel(1, "h1")),
        1698394659L, // GMT 2023.10.27 8:17:39
        false
    ), new StoryModel(
        1,
        "excited1",
        2,
        "title",
        "content",
        Arrays.asList(new HashtagModel(1, "h1"), new HashtagModel(2, "h2")),
        1698481059L, // GMT 2023.10.28 8:17:39
        false
    ), new StoryModel(
        1,
        "invalid",
        0,
        "title",
        "content",
        Arrays.asList(),
        1698567459L, // GMT 2023.10.28 8:17:39
        false
    ));

    @Test
    public void stat_success() {

        doAnswer(invocation -> {
            StoryGetCallBack callback = (StoryGetCallBack) invocation.getArguments()[3];
            callback.onSuccess(stories);
            return null;
        }).when(storyDataSource).getStory(anyString(), anyLong(), anyLong(), any());
        viewModel.getStat().observeForever(statState -> {
        });
        //when
        viewModel.getStats(testDay, false);
        //then
        LiveData<StatState> result = viewModel.getStat();
        //then
        Map<String, Integer> emotions = result.getValue().getEmotionCounts();
        Map<String, Integer> hashtags = result.getValue().getHashtagCounts();
        Map<Integer, Integer> scores = result.getValue().getScoresBydateOffset();

        assertEquals(Optional.ofNullable(2), Optional.ofNullable(emotions.get("설렘")));

        assertEquals(Optional.ofNullable(2), Optional.ofNullable(hashtags.get("h1")));
        assertEquals(Optional.of(1), Optional.ofNullable(hashtags.get("h2")));

        assertEquals(Optional.of(3), Optional.ofNullable(scores.get(7)));
        assertEquals(Optional.of(2), Optional.ofNullable(scores.get(6)));

        assertEquals(viewModel.getStartDate().getValue(), LocalDate.of(2023, 10, 27));
        assertEquals(viewModel.getEndDate().getValue(), testDay);
        assertEquals(viewModel.getAverageScore().getValue(), 2.5, 0.001);
    }
}
