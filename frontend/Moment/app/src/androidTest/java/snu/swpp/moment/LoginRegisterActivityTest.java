package snu.swpp.moment;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class LoginRegisterActivityTest {

    @Rule
    public ActivityScenarioRule<LoginRegisterActivity> activityScenarioRule
        = new ActivityScenarioRule<>(LoginRegisterActivity.class);

    @Test
    public void whenLoginRegisterActivityLaunched_TextLogoIsDisplayed() {
        onView(withId(R.id.imageView)).check(matches(isDisplayed()));
    }

    @Test
    public void whenLoginRegisterActivityLaunched_LoginButtonIsDisplayed() {
        onView(withId(R.id.main_login))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(withText(R.string.start_login)));
    }

    @Test
    public void whenLoginRegisterActivityLaunched_RegisterButtonIsDisplayed() {
        onView(withId(R.id.main_register))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(withText(R.string.start_register)));
    }
}