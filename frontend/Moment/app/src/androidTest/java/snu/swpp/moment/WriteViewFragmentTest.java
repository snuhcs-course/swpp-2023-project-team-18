package snu.swpp.moment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
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
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.ui.login.LoginActivity;
import snu.swpp.moment.utils.CustomViewAction;
import snu.swpp.moment.utils.TimeConverter;
import snu.swpp.moment.utils.ViewPager2Actions;

@RunWith(AndroidJUnit4.class)
public class WriteViewFragmentTest {

    private AuthenticationRepository authenticationRepository;
    private IdlingResource idlingResource;

    @Rule
    public ActivityScenarioRule<LoginActivity> scenarioRule = new ActivityScenarioRule<>(
        LoginActivity.class);

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws GeneralSecurityException, IOException {
        authenticationRepository = AuthenticationRepository.getInstance(
            ApplicationProvider.getApplicationContext());
        idlingResource = authenticationRepository.getIdlingResource();
        IdlingRegistry.getInstance().register(idlingResource);

        onView(withId(R.id.username)).perform(click(), replaceText("test"));
        onView(withId(R.id.password)).perform(click(), replaceText("123456"));

        // click()을 사용하면 알 수 없는 이유로 LoginActivy에 있는 login button의 listener가 불리지 않음.
        onView(withId(R.id.login_button))
            .check(matches(withText(R.string.action_log_in)))
            .perform(CustomViewAction.forceClick());
    }

    @After
    public void tearDown() {
        IdlingRegistry.getInstance().unregister(idlingResource);
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
        onView(allOf(withId(R.id.addButton), withEffectiveVisibility(Visibility.VISIBLE)))
            .perform(click());
        onView(allOf(withId(R.id.momentEditText), withEffectiveVisibility(Visibility.VISIBLE)))
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
