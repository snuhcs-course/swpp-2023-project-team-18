package snu.swpp.moment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import snu.swpp.moment.ui.register.RegisterActivity;

@RunWith(AndroidJUnit4.class)
public class RegisterActivityTest {
    @Rule
    public ActivityScenarioRule<RegisterActivity> activityScenarioRule
        = new ActivityScenarioRule<>(RegisterActivity.class);

    @Test
    public void whenRegisterActivityLaunched_AllViewsAreDisplayed() {
        onView(withId(R.id.image_above_edittext)).check(matches(isDisplayed()));
        onView(withId(R.id.register_username))
            .check(matches(isDisplayed()))
            .check(matches(withHint(R.string.register_id)));
        onView(withId(R.id.register_nickname))
            .check(matches(isDisplayed()))
            .check(matches(withHint(R.string.register_nickname)));
        onView(withId(R.id.register_password))
            .check(matches(isDisplayed()))
            .check(matches(withHint(R.string.password)));
        onView(withId(R.id.register_password_check))
            .check(matches(isDisplayed()))
            .check(matches(withHint(R.string.password_check)));
    }

    @Test
    public void whenUsernameEmpty_RegistrationIsDisabled() {
        onView(withId(R.id.register_nickname)).perform(typeText("moment"));
        onView(withId(R.id.register_password)).perform(typeText("000000"));
        onView(withId(R.id.register_password_check)).perform(typeText("000000"));

        onView(withId(R.id.register))
            .check(matches(not(isEnabled())));
    }

    @Test
    public void whenPasswordInvalid_RegistrationIsDisabled() {
        onView(withId(R.id.register_username)).perform(typeText("moment"));
        onView(withId(R.id.register_nickname)).perform(typeText("moment"));
        onView(withId(R.id.register_password)).perform(typeText("0"));
        onView(withId(R.id.register_password_check)).perform(typeText("0"));

        onView(withId(R.id.register))
            .check(matches(not(isEnabled())));
    }

    @Test
    public void whenPasswordAndPasswordCheckNotEqual_RegistrationIsDisabled() {
        onView(withId(R.id.register_username)).perform(typeText("moment1"));
        onView(withId(R.id.register_nickname)).perform(typeText("moment"));
        onView(withId(R.id.register_password)).perform(typeText("000000"));
        onView(withId(R.id.register_password_check)).perform(typeText("000001"));

        onView(withId(R.id.register))
            .check(matches(not(isEnabled())));
    }

    @Test
    public void whenAllFieldsValid_RegistrationIsEnabled() {
        onView(withId(R.id.register_username)).perform(click(), replaceText("moment"));
        onView(withId(R.id.register_nickname)).perform(click(), replaceText("moment"));
        onView(withId(R.id.register_password)).perform(click(), replaceText("123456"));
        onView(withId(R.id.register_password_check)).perform(click(), replaceText("123456"));

        onView(withId(R.id.register))
            .check(matches(isEnabled()));
    }
}
