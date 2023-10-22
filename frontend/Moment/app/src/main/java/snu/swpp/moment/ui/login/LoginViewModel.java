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
    private MutableLiveData<LoginResultState> loginResult = new MutableLiveData<>();
    private AuthenticationRepository authenticationRepository;

    LoginViewModel(AuthenticationRepository authenticationRepository) {
        this.authenticationRepository = authenticationRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResultState> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String password) {
        // can be launched in a separate asynchronous job
        System.out.println(
            "#Debug from ViewModel || username : " + username + "password : " + password);
        authenticationRepository.login(username, password, new AuthenticationCallBack() {
            @Override
            public void onSuccess(LoggedInUser loggedInUser) {
                System.out.println("#Debug from ViewModel HIHIHIHIHIHIHIHI");
                loginResult.setValue(
                    new LoginResultState(new LoggedInUserState(loggedInUser.getNickName())));
            }

            @Override
            public void onFailure(String errorMessage) {
                if (errorMessage.equals(
                    "{\"error\":[\"Unable to log in with provided credentials.\"]}")) {
                    loginResult.setValue(new LoginResultState(R.string.wrong_password));
                } else if (errorMessage.equals("Server")) {
                    loginResult.setValue(new LoginResultState(R.string.server_error));
                } else if (errorMessage.equals("NO INTERNET")) {
                    loginResult.setValue(new LoginResultState(R.string.internet_error));
                } else {
                    loginResult.setValue(new LoginResultState(R.string.unknown_error));
                }
            }
        });
    }

    public void loginDataChanged(String username, String password) {
        loginFormState.setValue(new LoginFormState(true));
    }
}