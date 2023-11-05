package snu.swpp.moment;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;


import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class LoginRegisterActivityTest {

    @Rule
    public ActivityScenarioRule<LoginRegisterActivity> activityScenarioRule = new ActivityScenarioRule<>(LoginRegisterActivity.class);

    @Test
    public void whenLoginButtonClicked_LoginActivityIsLaunched() {
        // 버튼을 찾아서 클릭 수행
        onView(withId(R.id.main_login)).perform(click());
        onView(withId(R.id.image_above_edittext)).check(matches(isDisplayed()));
    }

    @Test
    public void whenRegisterButtonClicked_LoginActivityIsLaunched() {
        // 버튼을 찾아서 클릭 수행
        onView(withId(R.id.main_register)).perform(click());
        onView(withId(R.id.image_above_edittext)).check(matches(isDisplayed()));
    }
}