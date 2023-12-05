package snu.swpp.moment.ui.main_userinfoview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import snu.swpp.moment.data.callback.NicknameCallBack;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.TokenModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.source.UserLocalDataSource;
import snu.swpp.moment.data.source.UserRemoteDataSource;
import snu.swpp.moment.exception.UnauthorizedAccessException;

@RunWith(MockitoJUnitRunner.class)
public class UserInfoViewModelTest {

    private UserInfoViewModel viewModel;

    @Mock
    private UserLocalDataSource localDataSource;
    @Mock
    private UserRemoteDataSource remoteDataSource;

    @Spy
    @InjectMocks
    private AuthenticationRepository repository;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() {
        doAnswer(invocation -> {
            TokenCallBack callback = (TokenCallBack) invocation.getArguments()[0];
            callback.onSuccess();
            return null;
        }).when(repository).isTokenValid(any());
        doReturn(new TokenModel("access", "refresh")).when(repository).getToken();

        viewModel = new UserInfoViewModel(repository);
    }

    @Test
    public void updateNickname_success() {
        // Given
        final String nickname = "nickname";
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            NicknameCallBack callback = (NicknameCallBack) args[2];
            callback.onSuccess(nickname);
            return null;
        }).when(remoteDataSource).updateNickname(anyString(), anyString(), any());

        viewModel.getNicknameUpdateErrorState().observeForever(errorState -> {
        });

        // When
        viewModel.updateNickname(nickname);

        // Then
        LiveData<NicknameUpdateErrorState> errorState = viewModel.getNicknameUpdateErrorState();
        System.out.println(errorState.getValue());
        assertNull(errorState.getValue().getError());
        assertEquals(nickname, viewModel.getNickname());
    }

    @Test
    public void updateNickname_fail() {
        // Given
        final String nickname = "nickname";
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            NicknameCallBack callback = (NicknameCallBack) args[2];
            callback.onFailure(new UnauthorizedAccessException());
            return null;
        }).when(remoteDataSource).updateNickname(anyString(), anyString(), any());

        viewModel.getNicknameUpdateErrorState().observeForever(errorState -> {
        });

        // When
        viewModel.updateNickname(nickname);

        // Then
        LiveData<NicknameUpdateErrorState> errorState = viewModel.getNicknameUpdateErrorState();
        assertNotNull(errorState.getValue().getError());
        assertEquals("토큰이 만료되었습니다.", errorState.getValue().getError().getMessage());
    }
}
