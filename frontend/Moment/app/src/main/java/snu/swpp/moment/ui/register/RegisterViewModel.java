package snu.swpp.moment.ui.register;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import snu.swpp.moment.R;
import snu.swpp.moment.data.AuthenticationCallBack;
import snu.swpp.moment.data.AuthenticationRepository;
import snu.swpp.moment.data.model.LoggedInUser;


public class RegisterViewModel extends ViewModel {

    private MutableLiveData<RegisterFormState> registerFormState = new MutableLiveData<>();
    private MutableLiveData<RegisterResult> registerResult = new MutableLiveData<>();

    //Repository
    private AuthenticationRepository authenticationRepository;

    RegisterViewModel(AuthenticationRepository authenticationRepository) {
        this.authenticationRepository = authenticationRepository;
    }

    LiveData<RegisterFormState> getRegisterFormState() {
        return registerFormState;
    }

    LiveData<RegisterResult> getRegisterResult() {
        return registerResult;
    }

    public void register(String username, String password, String nickname) {
        // can be launched in a separate asynchronous job
        authenticationRepository.register(username, password, nickname, new AuthenticationCallBack() {
            @Override
            public void onSuccess(LoggedInUser loggedInUser) {
                registerResult.setValue(new RegisterResult(new RegisterUserView(loggedInUser.getNickName())));
            }

            @Override
            public void onFailure(String errorMessage) {
                registerResult.setValue(new RegisterResult(R.string.register_failed));
            }
        });
    }

    public void registerDataChanged(String username, String password, String passwordCheck) {
        if (!isUserNameValid(username)) {
            registerFormState.setValue(new RegisterFormState(R.string.register_username, null,null));
        } else if (!isPasswordValid(password)) {
            registerFormState.setValue(new RegisterFormState(null, R.string.register_password, null));
        } else if (!isPasswordCheckValid(password, passwordCheck)){
            registerFormState.setValue(new RegisterFormState(null, null, R.string.register_password_check));
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

    //Password check
    private boolean isPasswordCheckValid(String password, String passwordCheck){
        return (password.equals(passwordCheck));
    }
}