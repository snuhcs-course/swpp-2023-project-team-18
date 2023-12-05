package snu.swpp.moment.ui.register;

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
public class RegisterViewModelTest extends TestCase {

    @Mock
    private UserRemoteDataSource userRemoteDataSource;

    @Mock
    private UserLocalDataSource userLocalDataSource;

    @Spy
    @InjectMocks
    private AuthenticationRepository authenticationRepository;

    private RegisterViewModel registerViewModel;

    // For testing with LiveData
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setup() {
        registerViewModel = new RegisterViewModel(authenticationRepository);
    }

    @Test
    public void register_success() {
        final LoggedInUserModel loggedInUser = new LoggedInUserModel(
            "test_username",
            "test_nickname",
            "yay",
            "yay",
            "yay"
        );
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            AuthenticationCallBack callback = (AuthenticationCallBack) args[3];
            callback.onSuccess(loggedInUser);
            return null;
        }).when(userRemoteDataSource).register(anyString(), anyString(), anyString(), any());

        Observer<RegisterResultState> registerResultStateObserver = RegisterResultState -> {
        };
        registerViewModel.getRegisterResult().observeForever(registerResultStateObserver);

        // When
        registerViewModel.register(loggedInUser.getUsername(), "yay", loggedInUser.getNickName());

        // Then
        LiveData<RegisterResultState> registerResultState = registerViewModel.getRegisterResult();

        assertEquals(registerResultState.getValue().getSuccess().getNickname(), "test_nickname");
    }

    @Test
    public void register_fail_dupUsername() {
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            AuthenticationCallBack callback = (AuthenticationCallBack) args[3];
            callback.onFailure("{\"username\":[\"A user with that username already exists.\"]}");
            return null;
        }).when(userRemoteDataSource).register(anyString(), anyString(), anyString(), any());

        Observer<RegisterResultState> registerResultStateObserver = RegisterResultState -> {
        };
        registerViewModel.getRegisterResult().observeForever(registerResultStateObserver);

        // When
        registerViewModel.register("", "", "");

        // Then
        LiveData<RegisterResultState> registerResultState = registerViewModel.getRegisterResult();

        assertEquals(Optional.ofNullable(registerResultState.getValue().getError()),
            Optional.ofNullable(R.string.username_error));
    }

    @Test
    public void register_fail_serverError() {
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            AuthenticationCallBack callback = (AuthenticationCallBack) args[3];
            callback.onFailure("Server");
            return null;
        }).when(userRemoteDataSource).register(anyString(), anyString(), anyString(), any());

        Observer<RegisterResultState> registerResultStateObserver = RegisterResultState -> {
        };
        registerViewModel.getRegisterResult().observeForever(registerResultStateObserver);

        // When
        registerViewModel.register("", "", "");

        // Then
        LiveData<RegisterResultState> registerResultState = registerViewModel.getRegisterResult();

        assertEquals(Optional.ofNullable(registerResultState.getValue().getError()),
            Optional.ofNullable(R.string.server_error));
    }

    @Test
    public void register_fail_internetError() {
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            AuthenticationCallBack callback = (AuthenticationCallBack) args[3];
            callback.onFailure("NO INTERNET");
            return null;
        }).when(userRemoteDataSource).register(anyString(), anyString(), anyString(), any());

        Observer<RegisterResultState> registerResultStateObserver = RegisterResultState -> {
        };
        registerViewModel.getRegisterResult().observeForever(registerResultStateObserver);

        // When
        registerViewModel.register("", "", "");

        // Then
        LiveData<RegisterResultState> registerResultState = registerViewModel.getRegisterResult();

        assertEquals(Optional.ofNullable(registerResultState.getValue().getError()),
            Optional.ofNullable(R.string.internet_error));
    }

    @Test
    public void register_fail_unknownError() {
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            AuthenticationCallBack callback = (AuthenticationCallBack) args[3];
            callback.onFailure("blahblahblah");
            return null;
        }).when(userRemoteDataSource).register(anyString(), anyString(), anyString(), any());

        Observer<RegisterResultState> registerResultStateObserver = RegisterResultState -> {
        };
        registerViewModel.getRegisterResult().observeForever(registerResultStateObserver);

        // When
        registerViewModel.register("", "", "");

        // Then
        LiveData<RegisterResultState> registerResultState = registerViewModel.getRegisterResult();

        assertEquals(Optional.ofNullable(registerResultState.getValue().getError()),
            Optional.ofNullable(R.string.unknown_error));
    }

    @Test
    public void registerDataChanged_userNameNull_setInvalidUsername() {
        Observer<RegisterFormState> registerFormStateObserver = RegisterFormState -> {
        };
        registerViewModel.getRegisterFormState().observeForever(registerFormStateObserver);

        registerViewModel.registerDataChanged(null, "", "");
        LiveData<RegisterFormState> registerFormState = registerViewModel.getRegisterFormState();

        assertEquals(Optional.ofNullable(registerFormState.getValue().getUsernameError()),
            Optional.ofNullable(R.string.register_username));
        assertNull(registerFormState.getValue().getPasswordError());
        assertNull(registerFormState.getValue().getPasswordDiffError());
    }

    @Test
    public void registerDataChanged_passwordNull_setInvalidPassword() {
        Observer<RegisterFormState> registerFormStateObserver = RegisterFormState -> {
        };
        registerViewModel.getRegisterFormState().observeForever(registerFormStateObserver);

        registerViewModel.registerDataChanged("test_username", null, "");
        LiveData<RegisterFormState> registerFormState = registerViewModel.getRegisterFormState();

        assertNull(registerFormState.getValue().getUsernameError());
        assertEquals(Optional.ofNullable(registerFormState.getValue().getPasswordError()),
            Optional.ofNullable(R.string.register_password));
        assertNull(registerFormState.getValue().getPasswordDiffError());
    }

    @Test
    public void registerDataChanged_passwordInvalid_setInvalidPassword() {
        Observer<RegisterFormState> registerFormStateObserver = RegisterFormState -> {
        };
        registerViewModel.getRegisterFormState().observeForever(registerFormStateObserver);

        registerViewModel.registerDataChanged("test_username", "yay", "");
        LiveData<RegisterFormState> registerFormState = registerViewModel.getRegisterFormState();

        assertNull(registerFormState.getValue().getUsernameError());
        assertEquals(Optional.ofNullable(registerFormState.getValue().getPasswordError()),
            Optional.ofNullable(R.string.register_password));
        assertNull(registerFormState.getValue().getPasswordDiffError());
    }

    @Test
    public void registerDataChanged_passwordCheckInvalid_setInvalidPasswordCheck() {
        Observer<RegisterFormState> registerFormStateObserver = RegisterFormState -> {
        };
        registerViewModel.getRegisterFormState().observeForever(registerFormStateObserver);

        registerViewModel.registerDataChanged("test_username", "yayayay", "");
        LiveData<RegisterFormState> registerFormState = registerViewModel.getRegisterFormState();

        assertNull(registerFormState.getValue().getUsernameError());
        assertNull(registerFormState.getValue().getPasswordError());
        assertEquals(Optional.ofNullable(registerFormState.getValue().getPasswordDiffError()),
            Optional.ofNullable(R.string.register_password_check));
    }

    @Test
    public void registerDataChanged_success() {
        Observer<RegisterFormState> registerFormStateObserver = RegisterFormState -> {
        };
        registerViewModel.getRegisterFormState().observeForever(registerFormStateObserver);

        registerViewModel.registerDataChanged("test_username", "yayayay", "yayayay");
        LiveData<RegisterFormState> registerFormState = registerViewModel.getRegisterFormState();

        assertEquals(registerFormState.getValue().isDataValid(), true);
    }
}