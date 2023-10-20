package snu.swpp.moment.utils;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class KeyboardUtils {

    public static void hideKeyboardOnOutsideTouch(View view, final Context context) {
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(context);
                    return false;
                }
            });
        }

        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                hideKeyboardOnOutsideTouch(innerView, context);
            }
        }
    }

    public static void hideSoftKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && ((AppCompatActivity) context).getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(((AppCompatActivity) context).getCurrentFocus().getWindowToken(), 0);
        }
    }
}
