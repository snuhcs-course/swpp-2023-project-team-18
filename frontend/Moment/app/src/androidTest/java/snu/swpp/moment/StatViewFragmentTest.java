package snu.swpp.moment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static snu.swpp.moment.utils.CustomViewActions.forceClick;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.compose.ui.platform.ComposeView;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import snu.swpp.moment.ui.login.LoginActivity;
import snu.swpp.moment.utils.LoginAction;
import snu.swpp.moment.utils.TimeConverter;

@RunWith(AndroidJUnit4.class)
public class StatViewFragmentTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> scenarioRule = new ActivityScenarioRule<>(
        LoginActivity.class);

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws GeneralSecurityException, IOException {
        LoginAction.setUp();
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_view))
            .perform(NavigationViewActions.navigateTo(R.id.StatView));
    }

    @After
    public void tearDown() {
        LoginAction.tearDown();
    }

    @Test
    public void whenStatViewFragmentLaunched_ScoreGraphIsDisplayed() {
        onView(withId(R.id.stat_line_chart)).check(matches(isDisplayed()));
        onView(withId(R.id.average_score_text)).check(matches(withText(R.string.stat_seven_avg)));
        onView(withId(R.id.score_unit)).check(matches(withText("점")));
    }

    @Test
    public void whenStatViewFragmentLaunched_EmotionChartIsDisplayed() {
        onView(withId(R.id.stat_pie_chart))
            .check(matches(isDisplayed()));
    }

    @Test
    public void whenStatViewFragmentScrolledDown_WordCloudIsDisplayed() {
        // 알 수 없는 이유로 word cloud은 xml에 id가 있지만 id로 찾을 수 없음.
        // 따라서 어떤 클래스의 뷰인지로 확인
        onView(isAssignableFrom(ComposeView.class))
            .perform(scrollTo()) // 워드 클라우드는 맨밑에 있어 보이지 않기 때문에 스크롤 필요
            .check(matches(isDisplayed()));
    }

    @Test
    public void whenMonthOptionButtonClicked_DurationTextChangesAccordingly() {
        String weekBefore = getStartDateInString(7);
        String monthBefore = getStartDateInString(30);
        onView(withId(R.id.stat_date_duration_start))
            .check(matches(withText(weekBefore)));

        onView(withId(R.id.stat_monthButton)).perform(forceClick());
        onView(withId(R.id.stat_date_duration_start))
            .check(matches(withText(monthBefore)));
    }

    private String getStartDateInString(int offset) {
        LocalDate date = TimeConverter.getToday().minusDays(offset);
        return date.format(DateTimeFormatter.ofPattern("yy.MM.dd"));
    }

}
