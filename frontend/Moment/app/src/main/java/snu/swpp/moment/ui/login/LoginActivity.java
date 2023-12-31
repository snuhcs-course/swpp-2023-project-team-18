package snu.swpp.moment.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import snu.swpp.moment.MainActivity;
import snu.swpp.moment.databinding.ActivityLoginBinding;
import snu.swpp.moment.utils.KeyboardUtils;


public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            loginViewModel = new ViewModelProvider(this,
                new LoginViewModelFactory(getApplicationContext()))
                .get(LoginViewModel.class);
        } catch (Exception e) {
            Toast.makeText(this, "보안용 파일을 만드는데 실패하였습니다. 개발자에게 연락하세요.", Toast.LENGTH_SHORT).show();
        }

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.loginButton;
        final ProgressBar loadingProgressBar = binding.loading;

        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState.isDataValid());
            loginButton.setActivated(loginFormState.isDataValid());
            if (loginFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(loginFormState.getUsernameError()));
            }
            if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()));
            }
        });

        loginViewModel.getLoginResult().observe(this, loginResult -> {
            if (loginResult == null) {
                return;
            }
            loadingProgressBar.setVisibility(View.GONE);
            if (loginResult.getError() != null) {
                showLoginFailed(loginResult.getError());
            }
            if (loginResult.getSuccess() != null) {
                System.out.println("#DEBUG : ACTIVITY HIHIHII");

                //updateUiWithUser(loginResult.getSuccess());

                Intent testLoginSuccess = new Intent(LoginActivity.this, MainActivity.class);
                System.out.println("#DEBUG : ACTIVITY @@@@@@");
                startActivity(testLoginSuccess);
            }
            setResult(Activity.RESULT_OK);

            //Complete and destroy login activity once successful
            //finish();
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                    passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            /*
             * Login
             */
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
                    return true;  // Consume the event
                }
                return false;
            }
        });

        loginButton.setOnClickListener(
            v -> {
                Log.d("LoginActivity", "loginButtonClicked");
                loginViewModel.login(
                    usernameEditText.getText().toString(),
                    passwordEditText.getText().toString()
                );
            }
        );
        View root = binding.getRoot();
        KeyboardUtils.hideKeyboardOnOutsideTouch(root, this);
    }


    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}