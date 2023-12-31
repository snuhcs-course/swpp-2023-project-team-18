package snu.swpp.moment.ui.register;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import snu.swpp.moment.R;
import snu.swpp.moment.databinding.ActivityRegisterBinding;
import snu.swpp.moment.utils.KeyboardUtils;

public class RegisterActivity extends AppCompatActivity {

    private RegisterViewModel registerViewModel;
    private ActivityRegisterBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        registerViewModel = new ViewModelProvider(this,
            new RegisterViewModelFactory(getApplicationContext())).get(RegisterViewModel.class);

        final EditText usernameEditText = binding.registerUsername;
        final EditText passwordEditText = binding.registerPassword;
        final EditText passwordCheckEditText = binding.registerPasswordCheck;
        final EditText nicknameEditText = binding.registerNickname;
        final Button registerButton = binding.register;
        final ProgressBar loadingProgressBar = binding.loading;

        registerViewModel.getRegisterFormState().observe(this, registerFormState -> {
            if (registerFormState == null) {
                return;
            }
            registerButton.setEnabled(registerFormState.isDataValid());
            registerButton.setActivated(registerFormState.isDataValid());
            if (registerFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(registerFormState.getUsernameError()));
            }
            if (registerFormState.getPasswordError() != null) {
                passwordCheckEditText.setError(getString(registerFormState.getPasswordError()));
            }
            if (registerFormState.getPasswordDiffError() != null) {
                passwordCheckEditText.setError(
                    getString(registerFormState.getPasswordDiffError()));
            }
        });

        registerViewModel.getRegisterResult().observe(this, registerResult -> {
            if (registerResult == null) {
                Toast.makeText(RegisterActivity.this, R.string.unknown_error,
                        Toast.LENGTH_SHORT)
                    .show();
            }
            //loadingProgressBar.setVisibility(View.GONE);
            if (registerResult.getError() != null) {
                showLoginFailed(registerResult.getError());
            }
            if (registerResult.getSuccess() != null) {
                updateUiWithUser(registerResult.getSuccess());
                Intent registerIntent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(registerIntent);
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
                registerViewModel.registerDataChanged(usernameEditText.getText().toString(),
                    passwordEditText.getText().toString(),
                    passwordCheckEditText.getText().toString());
                // Check if the password and password check are not the same
                if (!passwordEditText.getText().toString().isEmpty() &&
                    !passwordCheckEditText.getText().toString().isEmpty() &&
                    !passwordEditText.getText().toString()
                        .equals(passwordCheckEditText.getText().toString())) {
                    passwordCheckEditText.setError(
                        getResources().getString(R.string.register_password_check)
                    );
                }
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordCheckEditText.addTextChangedListener(afterTextChangedListener);

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    registerViewModel.register(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(),
                        nicknameEditText.getText().toString());
                }
                return false;
            }
        });

        registerButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String nickname = nicknameEditText.getText().toString();

            registerViewModel.register(username, password, nickname);
        });

        View root = binding.getRoot();
        KeyboardUtils.hideKeyboardOnOutsideTouch(root, this);
    }

    private void updateUiWithUser(RegisterUserState model) {
        String welcome = model.getNickname() + "님 환영합니다!";
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}