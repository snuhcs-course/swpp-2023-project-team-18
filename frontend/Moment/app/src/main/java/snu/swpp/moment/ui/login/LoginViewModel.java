package snu.swpp.moment.ui.login;

import android.util.Patterns;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import snu.swpp.moment.R;
import snu.swpp.moment.data.callback.AuthenticationCallBack;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.model.LoggedInUser;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private AuthenticationRepository loginRepository;

    LoginViewModel(AuthenticationRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String password) {
        // can be launched in a separate asynchronous job
        System.out.println(
            "#Debug from ViewModel || username : " + username + "password : " + password);
        loginRepository.login(username, password, new AuthenticationCallBack() {
            @Override
            public void onSuccess(LoggedInUser loggedInUser) {
                System.out.println("#Debug from ViewModel HIHIHIHIHIHIHIHI");
                loginResult.setValue(
                    new LoginResult(new LoggedInUserView(loggedInUser.getNickName())));
            }

            @Override
            public void onFailure(String errorMessage) {
                if (errorMessage.equals(
                    "{\"error\":[\"Unable to log in with provided credentials.\"]}")) {
                    loginResult.setValue(new LoginResult(R.string.wrong_password));
                } else if (errorMessage.equals("Server")) {
                    loginResult.setValue(new LoginResult(R.string.server_error));
                } else if (errorMessage.equals("NO INTERNET")) {
                    loginResult.setValue(new LoginResult(R.string.internet_error));
                } else {
                    loginResult.setValue(new LoginResult(R.string.unknown_error));
                }
            }
        });
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}