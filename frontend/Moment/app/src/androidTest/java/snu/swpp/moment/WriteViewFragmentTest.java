package snu.swpp.moment;

import static androidx.core.content.ContextCompat.startActivity;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.NavigationViewActions.navigateTo;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import android.content.Intent;
import android.util.Log;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import snu.swpp.moment.data.callback.AuthenticationCallBack;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.LoggedInUserModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.ui.login.LoginActivity;
import snu.swpp.moment.ui.main_writeview.viewmodel.TodayViewModel;
import snu.swpp.moment.utils.TimeConverter;

@RunWith(AndroidJUnit4.class)
public class WriteViewFragmentTest {
    private AuthenticationRepository authenticationRepository;
    private IdlingResource idlingResource;

    @Rule
    public ActivityScenarioRule<LoginActivity> scenarioRule = new ActivityScenarioRule<>(LoginActivity.class);

    @Before
    public void setUp() throws GeneralSecurityException, IOException {
        authenticationRepository = AuthenticationRepository.getInstance(ApplicationProvider.getApplicationContext());
        idlingResource = authenticationRepository.getIdlingResource();
        IdlingRegistry.getInstance().register();

        onView(withId(R.id.username)).perform(click(), replaceText("test"));
        onView(withId(R.id.password)).perform(click(), replaceText("123456"));
        onView(withId(R.id.login_button)).perform(click());
        scenarioRule.getScenario().onActivity(activity -> {
            activity.startActivity(new Intent(activity, MainActivity.class));
        });
    }

    @After
    public void tearDown() {
        IdlingRegistry.getInstance().unregister(idlingResource);
    }

    @Test
    public void whenTodayViewFragmentLaunched_ToolBarTitleDisplaysToday() {
        LocalDate today = TimeConverter.getToday();
        String formattedDate = TimeConverter.formatLocalDate(today, "yyyy. MM. dd.");
        String dayOfWeek = today.getDayOfWeek().getDisplayName(
            java.time.format.TextStyle.SHORT, java.util.Locale.KOREAN);
        String title = formattedDate + ' ' + dayOfWeek;
        onView(withId(R.id.text_title)).check(matches(withText(title)));
    }

    @Test
    public void whenTodayViewFragmentLaunched_BackToTodayButtonIsInvisible() {
        onView(withId(R.id.backToTodayButton)).check(matches(not(isDisplayed())));
    }

    @Test
    public void whenTodayViewFragmentLaunched_MomentEditTextIsInvisible() {
        onView(allOf(withId(R.id.momentEditText))).check(matches(not(isDisplayed())));
    }

    @Test
    public void whenAddButtonClicked_EditTextIsDisplayed() {
        // TODO
        onView(allOf(withId(R.id.addButton)))
            .perform(click())
            .check(matches(not(isDisplayed())));
        onView(anyOf(withId(R.id.momentEditText))).check(matches(isDisplayed()));
    }
}
