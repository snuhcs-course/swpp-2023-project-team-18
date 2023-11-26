package snu.swpp.moment.utils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static snu.swpp.moment.utils.CustomViewActions.forceClick;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import snu.swpp.moment.R;
import snu.swpp.moment.data.repository.AuthenticationRepository;

public class LoginAction {

    private static final String username = "test";
    private static final String password = "123456";
    private static IdlingResource idlingResource;

    public static void setUp() {
        registerIdlingResource();
        login();
    }

    public static void tearDown() {
        IdlingRegistry.getInstance().unregister(idlingResource);
    }

    private static void registerIdlingResource() {
        AuthenticationRepository authenticationRepository = AuthenticationRepository.getInstance(
            ApplicationProvider.getApplicationContext());
        idlingResource = authenticationRepository.getIdlingResource();
        IdlingRegistry.getInstance().register(idlingResource);
    }

    private static void login() {
        onView(withId(R.id.username)).perform(new DisableAutoFillAction(), click(),
            replaceText(username));
        onView(withId(R.id.password)).perform(new DisableAutoFillAction(), click(),
            replaceText(password));
        onView(withId(R.id.login_button)).perform(forceClick());
    }
}
