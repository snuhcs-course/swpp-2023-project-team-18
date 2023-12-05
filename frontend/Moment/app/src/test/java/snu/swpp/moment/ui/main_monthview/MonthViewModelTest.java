package snu.swpp.moment.ui.main_monthview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

@RunWith(MockitoJUnitRunner.class)
public class MonthViewModelTest {

    private MonthViewModel viewModel;

    @Mock
    private StoryRemoteDataSource storyDataSource;

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

        viewModel = new MonthViewModel(authenticationRepository, storyRepository);
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
            1698999459L, // GMT 2023.11.3 8:17:39
            false
        );
        doAnswer(invocation -> {
            StoryGetCallBack callback = (StoryGetCallBack) invocation.getArguments()[3];
            callback.onSuccess(Arrays.asList(story));
            return null;
        }).when(storyDataSource).getStory(anyString(), anyLong(), anyLong(), any());

        Observer<CalendarMonthState> observer = calendarMonthState -> {
            // Then
            System.out.println("observer for getStory success");
            List<CalendarDayState> monthList = calendarMonthState.getStoryList();
            CalendarDayState day1state = calendarMonthState.getStoryList().get(0);
            CalendarDayState day3state = calendarMonthState.getStoryList().get(2);
            assertNull(calendarMonthState.getError());
            assertEquals(31, monthList.size());
            assertTrue(day1state.isEmotionInvalid());
            assertFalse(day3state.isEmotionInvalid());
        };
        viewModel.observerCalendarMonthState(observer);

        viewModel.getStory(YearMonth.of(2023, 11));
    }
}

