package snu.swpp.moment.ui.login;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import java.util.Optional;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import snu.swpp.moment.R;
import snu.swpp.moment.data.callback.AuthenticationCallBack;
import snu.swpp.moment.data.model.LoggedInUserModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.source.UserLocalDataSource;
import snu.swpp.moment.data.source.UserRemoteDataSource;

@RunWith(MockitoJUnitRunner.class)
public class LoginViewModelTest extends TestCase {

    @Mock
    private UserRemoteDataSource userRemoteDataSource;

    @Mock
    private UserLocalDataSource userLocalDataSource;

    @Spy
    @InjectMocks
    private AuthenticationRepository authenticationRepository;

    private LoginViewModel loginViewModel;

    // For testing with LiveData
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setup() {
        loginViewModel = new LoginViewModel(authenticationRepository);
    }

    @Test
    public void login_success() {
        final LoggedInUserModel loggedInUser = new LoggedInUserModel(
            "test_username",
            "test_nickname",
            "yay",
            "yay",
            "yay"
        );
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            AuthenticationCallBack callback = (AuthenticationCallBack) args[2];
            callback.onSuccess(loggedInUser);
            return null;
        }).when(userRemoteDataSource).login(anyString(), anyString(), any());

        Observer<LoginResultState> loginResultStateObserver = LoginResultState -> {
        };
        loginViewModel.getLoginResult().observeForever(loginResultStateObserver);

        // When
        loginViewModel.login(loggedInUser.getUsername(), "yay");

        // Then
        LiveData<LoginResultState> loginResultState = loginViewModel.getLoginResult();

        assertEquals(loginResultState.getValue().getSuccess().getNickname(), "test_nickname");
    }

    @Test
    public void login_fail_wrongPassword() {
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            AuthenticationCallBack callback = (AuthenticationCallBack) args[2];
            callback.onFailure("{\"error\":[\"Unable to log in with provided credentials.\"]}");
            return null;
        }).when(userRemoteDataSource).login(anyString(), anyString(), any());

        Observer<LoginResultState> loginResultStateObserver = LoginResultState -> {
        };
        loginViewModel.getLoginResult().observeForever(loginResultStateObserver);

        // When
        loginViewModel.login("", "");

        // Then
        LiveData<LoginResultState> loginResultState = loginViewModel.getLoginResult();

        assertEquals(Optional.ofNullable(loginResultState.getValue().getError()),
            Optional.ofNullable(R.string.wrong_password));
    }

    @Test
    public void login_fail_serverError() {
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            AuthenticationCallBack callback = (AuthenticationCallBack) args[2];
            callback.onFailure("Server");
            return null;
        }).when(userRemoteDataSource).login(anyString(), anyString(), any());

        Observer<LoginResultState> loginResultStateObserver = LoginResultState -> {
        };
        loginViewModel.getLoginResult().observeForever(loginResultStateObserver);

        // When
        loginViewModel.login("", "");

        // Then
        LiveData<LoginResultState> loginResultState = loginViewModel.getLoginResult();

        assertEquals(Optional.ofNullable(loginResultState.getValue().getError()),
            Optional.ofNullable(R.string.server_error));
    }

    @Test
    public void login_fail_internetError() {
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            AuthenticationCallBack callback = (AuthenticationCallBack) args[2];
            callback.onFailure("NO INTERNET");
            return null;
        }).when(userRemoteDataSource).login(anyString(), anyString(), any());

        Observer<LoginResultState> loginResultStateObserver = LoginResultState -> {
        };
        loginViewModel.getLoginResult().observeForever(loginResultStateObserver);

        // When
        loginViewModel.login("", "");

        // Then
        LiveData<LoginResultState> loginResultState = loginViewModel.getLoginResult();

        assertEquals(Optional.ofNullable(loginResultState.getValue().getError()),
            Optional.ofNullable(R.string.internet_error));
    }

    @Test
    public void login_fail_unknownError() {
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            AuthenticationCallBack callback = (AuthenticationCallBack) args[2];
            callback.onFailure("blahblahblah");
            return null;
        }).when(userRemoteDataSource).login(anyString(), anyString(), any());

        Observer<LoginResultState> loginResultStateObserver = LoginResultState -> {
        };
        loginViewModel.getLoginResult().observeForever(loginResultStateObserver);

        // When
        loginViewModel.login("", "");

        // Then
        LiveData<LoginResultState> loginResultState = loginViewModel.getLoginResult();

        assertEquals(Optional.ofNullable(loginResultState.getValue().getError()),
            Optional.ofNullable(R.string.unknown_error));
    }

    @Test
    public void loginDataChanged_success() {
        Observer<LoginFormState> loginFormStateObserver = LoginFormState -> {
        };
        loginViewModel.getLoginFormState().observeForever(loginFormStateObserver);

        loginViewModel.loginDataChanged("", "");
        LiveData<LoginFormState> loginFormState = loginViewModel.getLoginFormState();

        assertEquals(loginFormState.getValue().isDataValid(), true);
    }
}