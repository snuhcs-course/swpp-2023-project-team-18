package snu.swpp.moment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import snu.swpp.moment.ui.login.LoginActivity;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {
    @Rule
    public ActivityScenarioRule<LoginActivity> activityScenarioRule
        = new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void whenLoginActivityLaunched_TextLogoIsDisplayed() {
        onView(withId(R.id.image_above_edittext)).check(matches(isDisplayed()));
    }

    @Test
    public void whenLoginActivityLaunched_UsernameFieldIsDisplayed() {
        onView(withId(R.id.username))
            .check(matches(isDisplayed()))
            .check(matches(withHint(R.string.prompt_email)));
    }

    @Test
    public void whenLoginActivityLaunched_PasswordFieldIsDisplayed() {
        onView(withId(R.id.password))
            .check(matches(isDisplayed()))
            .check(matches(withHint(R.string.prompt_password)));
    }

    @Test
    public void whenLoginActivityLaunched_LoginButtonIsDisplayed() {
        onView(withId(R.id.login_button))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()));
    }
}
