package snu.swpp.moment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import snu.swpp.moment.ui.login.LoginActivity;
import snu.swpp.moment.utils.LoginAction;

@RunWith(AndroidJUnit4.class)
public class MonthViewFragmentTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> scenarioRule = new ActivityScenarioRule<>(
        LoginActivity.class);

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() {
        LoginAction.setUp();
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_view))
            .perform(NavigationViewActions.navigateTo(R.id.MonthView));
    }

    @After
    public void tearDown() {
        LoginAction.tearDown();
    }

    @Test
    public void whenMonthViewFragmentLaunched_ToolBarTitleDisplaysCurrentMonth() {
        YearMonth yearMonth = YearMonth.now();
        onView(withId(R.id.text_title)).check(matches(withText(getTitle(yearMonth))));
    }

    @Test
    public void whenMonthViewFragmentLaunched_CalendarIsDisplayed() {
        onView(withId(R.id.calendarView)).check(matches(isDisplayed()));
    }

    @Test
    public void whenSwipedRightOnce_ToolBarTitleDisplaysLastMonth() throws InterruptedException {
        onView(withId(R.id.calendarView)).perform(swipeRight());
        Thread.sleep(1000); // swipe할 때 시간이 걸려서 toolbar title을 바로 확인하면 오류
        YearMonth yearMonth = YearMonth.now().minusMonths(1);
        onView(withId(R.id.text_title)).check(matches(withText(getTitle(yearMonth))));
    }

    private String getTitle(YearMonth yearMonth) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. MM.");
        return yearMonth.format(formatter);
    }
}
