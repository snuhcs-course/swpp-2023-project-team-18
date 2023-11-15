package snu.swpp.moment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.NavigationViewActions.navigateTo;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import snu.swpp.moment.utils.TimeConverter;

@RunWith(AndroidJUnit4.class)
public class WriteViewFragmentTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(navigateTo(R.id.WriteView));
    }

    @Test
    public void whenTodayViewFragmentLaunched_ToolBarTitleDisplaysToday() {
        LocalDate today = TimeConverter.getToday();
        String formattedToday = TimeConverter.formatLocalDate(today, "yyyy. MM. dd.");
        onView(withId(R.id.text_title)).check(matches(withText(formattedToday)));
    }

    @Test
    public void whenTodayViewFragmentLaunched_BackToTodayButtonIsInvisible() {
        onView(withId(R.id.backToTodayButton)).check(matches(not(isDisplayed())));
    }

    @Test
    public void whenTodayViewFragmentLaunched_MomentEditTextIsInvisible() {
        onView(withId(R.id.momentEditText)).check(matches(not(isDisplayed())));
    }

    @Test
    public void whenAddButtonClicked_EditTextIsDisplayed() {
        onView(withId(R.id.addButton))
            .perform(click())
            .check(matches(not(isDisplayed())));
        onView(withId(R.id.momentEditText)).check(matches(isDisplayed()));
    }
}
