package snu.swpp.moment.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import snu.swpp.moment.R;
import snu.swpp.moment.data.callback.AuthenticationCallBack;
import snu.swpp.moment.data.model.LoggedInUserModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private final MutableLiveData<LoginResultState> loginResult = new MutableLiveData<>();
    private final AuthenticationRepository authenticationRepository;

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
            public void onSuccess(LoggedInUserModel loggedInUser) {
                System.out.println("#Debug from ViewModel HIHIHIHIHIHIHIHI");
                loginResult.setValue(
                    new LoginResultState(new LoggedInUserState(loggedInUser.getNickName())));
            }

            @Override
            public void onFailure(String errorMessage) {
                switch (errorMessage) {
                    case "{\"error\":[\"Unable to log in with provided credentials.\"]}":
                        loginResult.setValue(new LoginResultState(R.string.wrong_password));
                        break;
                    case "Server":
                        loginResult.setValue(new LoginResultState(R.string.server_error));
                        break;
                    case "NO INTERNET":
                        loginResult.setValue(new LoginResultState(R.string.internet_error));
                        break;
                    default:
                        loginResult.setValue(new LoginResultState(R.string.unknown_error));
                        break;
                }
            }
        });
    }

    public void loginDataChanged(String username, String password) {
        loginFormState.setValue(new LoginFormState(true));
    }
}