package snu.swpp.moment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import snu.swpp.moment.ui.login.LoginActivity;
import snu.swpp.moment.utils.LoginAction;
import snu.swpp.moment.utils.TimeConverter;
import snu.swpp.moment.utils.ViewPager2Actions;

@RunWith(AndroidJUnit4.class)
public class WriteViewFragmentTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> scenarioRule = new ActivityScenarioRule<>(
        LoginActivity.class);

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws GeneralSecurityException, IOException {
        LoginAction.setUp();
    }

    @After
    public void tearDown() {
        LoginAction.tearDown();
    }

    @Test
    public void whenTodayViewFragmentLaunched_ToolBarTitleDisplaysToday() {
        String title = getTitle(0);
        onView(withId(R.id.text_title)).check(matches(withText(title)));
    }

    @Test
    public void whenTodayViewFragmentLaunched_BackToTodayButtonIsInvisible() {
        onView(withId(R.id.backToTodayButton)).check(matches(not(isDisplayed())));
    }

    @Test
    public void whenAddButtonClicked_MomentEditTextIsClickable() {
        // 미리 만들어지는 fragment마다 addButton이 만들어지기 때문에 그 중에 현재 보이는 것만을 클릭
        onView(allOf(withId(R.id.addButton), isDisplayed()))
            .perform(click());
        onView(allOf(withId(R.id.momentEditText), isDisplayed()))
            .check(matches(isClickable()));
    }

    @Test
    public void whenPageScrolledToLeft_DailyViewFragmentIsLaunched() {
        String title = getTitle(1);
        onView(withId(R.id.viewpager)).perform(ViewPager2Actions.scrollLeft());
        onView(withId(R.id.text_title)).check(matches(withText(title)));
        onView(withId(R.id.backToTodayButton)).check(matches(isDisplayed()));
    }

    private String getTitle(int offset) {
        LocalDate day = TimeConverter.getToday().minusDays(offset);
        String formattedDate = TimeConverter.formatLocalDate(day, "yyyy. MM. dd.");
        String dayOfWeek = day.getDayOfWeek().getDisplayName(
            java.time.format.TextStyle.SHORT, java.util.Locale.KOREAN);
        return formattedDate + ' ' + dayOfWeek;
    }
}
