package snu.swpp.moment.utils;

import android.os.Build;
import android.view.View;
import android.view.autofill.AutofillManager;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

public class DisableAutoFillAction implements ViewAction {

    @Override
    public Matcher<View> getConstraints() {
        return Matchers.any(View.class);
    }

    @Override
    public String getDescription() {
        return "Dismissing autofill picker";
    }

    @Override
    public void perform(UiController uiController, View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            AutofillManager autofillManager = view.getContext()
                .getSystemService(AutofillManager.class);

            if (autofillManager != null) {
                autofillManager.cancel();
            }
        }
    }
}