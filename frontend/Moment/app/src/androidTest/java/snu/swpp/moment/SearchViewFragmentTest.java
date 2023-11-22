package snu.swpp.moment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;
import static snu.swpp.moment.utils.CustomViewActions.forceClick;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import snu.swpp.moment.ui.login.LoginActivity;
import snu.swpp.moment.utils.LoginAction;

@RunWith(AndroidJUnit4.class)
public class SearchViewFragmentTest {

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
            .perform(NavigationViewActions.navigateTo(R.id.SearchView));
    }

    @After
    public void tearDown() {
        LoginAction.tearDown();
    }

    @Test
    public void whenSearchViewFragmentLaunched_EditTextForHashtagSearchIsDisplayed() {
        onView(withId(R.id.search_hashtag_edittext))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()));
        onView(withId(R.id.search_content_edittext))
            .check(matches(not(isDisplayed())));
        onView(withId(R.id.search_content_query_button)).check(matches(not(isDisplayed())));
    }

    @Test
    public void whenContentButtonClicked_EditTextForContentSearchIsDisplayed() {
        onView(withId(R.id.search_content_button)).perform(forceClick());

        onView(withId(R.id.search_hashtag_edittext)).check(matches(not(isDisplayed())));
        onView(withId(R.id.search_content_edittext))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()));
        onView(withId(R.id.search_content_query_button)).check(matches(isDisplayed()));
    }
}
