package snu.swpp.moment.ui.register;

import android.util.Patterns;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import snu.swpp.moment.R;
import snu.swpp.moment.data.callback.AuthenticationCallBack;
import snu.swpp.moment.data.model.LoggedInUserModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;


public class RegisterViewModel extends ViewModel {

    private final MutableLiveData<RegisterFormState> registerFormState = new MutableLiveData<>();
    private final MutableLiveData<RegisterResultState> registerResult = new MutableLiveData<>();

    //Repository
    private final AuthenticationRepository authenticationRepository;

    RegisterViewModel(AuthenticationRepository authenticationRepository) {
        this.authenticationRepository = authenticationRepository;
    }

    LiveData<RegisterFormState> getRegisterFormState() {
        return registerFormState;
    }

    LiveData<RegisterResultState> getRegisterResult() {
        return registerResult;
    }

    public void register(String username, String password, String nickname) {
        // can be launched in a separate asynchronous job
        authenticationRepository.register(username, password, nickname,
            new AuthenticationCallBack() {
                @Override
                public void onSuccess(LoggedInUserModel loggedInUser) {
                    registerResult.setValue(
                        new RegisterResultState(new RegisterUserState(loggedInUser.getNickName())));
                }

                @Override
                public void onFailure(String errorMessage) {
                    System.out.println("#MESSAGE: " + errorMessage);
                    switch (errorMessage) {
                        case "{\"username\":[\"A user with that username already exists.\"]}":
                            registerResult.setValue(
                                new RegisterResultState(R.string.username_error));
                            break;
                        case "Server":
                            registerResult.setValue(new RegisterResultState(R.string.server_error));
                            break;
                        case "NO INTERNET":
                            registerResult.setValue(
                                new RegisterResultState(R.string.internet_error));
                            break;
                        default:
                            registerResult.setValue(
                                new RegisterResultState(R.string.unknown_error));
                            break;
                    }
                }
            });
    }

    public void registerDataChanged(String username, String password, String passwordCheck) {
        if (!isUserNameValid(username)) {
            registerFormState.setValue(
                new RegisterFormState(R.string.register_username, null, null));
        } else if (!isPasswordValid(password)) {
            registerFormState.setValue(
                new RegisterFormState(null, R.string.register_password, null));
        } else if (!isPasswordCheckValid(password, passwordCheck)) {
            registerFormState.setValue(
                new RegisterFormState(null, null, R.string.register_password_check));
        } else {
            registerFormState.setValue(new RegisterFormState(true));
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

    // Password check
    private boolean isPasswordCheckValid(String password, String passwordCheck) {
        return (password.equals(passwordCheck));
    }

    public void observeRegisterFormState(Observer<RegisterFormState> observer) {
        registerFormState.observeForever(observer);
    }

    public void observeRegisterResultState(Observer<RegisterResultState> observer) {
        registerResult.observeForever(observer);
    }
}