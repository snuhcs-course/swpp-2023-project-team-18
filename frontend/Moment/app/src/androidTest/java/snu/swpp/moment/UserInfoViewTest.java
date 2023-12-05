package snu.swpp.moment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isNotEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static org.hamcrest.Matchers.equalTo;
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
public class UserInfoViewTest {

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
            .perform(NavigationViewActions.navigateTo(R.id.UserInfoView));
    }

    @After
    public void tearDown() {
        LoginAction.tearDown();
    }

    @Test
    public void whenUserInfoViewFragmentLaunched_ViewComponentsAreDisplayed() {
        onView(withId(R.id.user_info_wrapper)).check(matches(isDisplayed()));
        onView(withId(R.id.logout_button)).check(matches(isDisplayed()));
        onView(withId(R.id.logo)).check(matches(isDisplayed()));
    }

    @Test
    public void whenPenIconClicked_EditModeIsActivated() {
        onView(withId(R.id.pen_icon)).perform(forceClick());
        // pen icon이 저장 버튼으로 바뀜
        onView(withId(R.id.pen_icon))
            .check(matches(withTagValue(equalTo(R.drawable.moment_write_button))));
        // 닉네임 입력창이 clickable 상태가 됨
        onView(withId(R.id.nickname_edittext)).check(matches(isEnabled()));
    }

    @Test
    public void whenPenIconClickedTwice_ViewModeIsActivated() {
        onView(withId(R.id.pen_icon)).perform(forceClick());
        onView(withId(R.id.pen_icon)).perform(forceClick());
        // pen icon이 펜 모양으로 바뀜
        onView(withId(R.id.pen_icon))
            .check(matches(withTagValue(equalTo(R.drawable.pen))));
        // 닉네임 입력창이 not clickable 상태가 됨
        onView(withId(R.id.nickname_edittext)).check(matches(isNotEnabled()));
    }

    @Test
    public void whenNicknameLengthLimitExceeded_WarningIsDisplayed() {
        onView(withId(R.id.pen_icon)).perform(forceClick());
        onView(withId(R.id.nickname_edittext)).perform(forceClick(),
            replaceText("닉네임!닉네임!닉네임!닉네임!닉네임!닉네임!"));
        // 저장 버튼이 비활성화됨
        onView(withId(R.id.pen_icon))
            .check(matches(withTagValue(equalTo(R.drawable.moment_write_inactivate))));
        // 경고 문구 표시됨
        onView(withId(R.id.nickname_length_warning_text)).check(matches(isDisplayed()));
    }
}
