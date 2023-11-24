package snu.swpp.moment.ui.main_userinfoview;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.time.LocalDate;
import snu.swpp.moment.data.callback.NicknameCallBack;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.exception.UnauthorizedAccessException;

public class UserInfoViewModel extends ViewModel {

    private String nickname;
    private final AuthenticationRepository repository;
    private final MutableLiveData<NicknameUpdateErrorState> nicknameUpdateErrorState;

    public UserInfoViewModel(AuthenticationRepository repository) {
        this.repository = repository;
        this.nickname = repository.getNickname();
        this.nicknameUpdateErrorState = new MutableLiveData<>();
        nicknameUpdateErrorState.setValue(new NicknameUpdateErrorState(null));
    }

    public LiveData<NicknameUpdateErrorState> getNicknameUpdateErrorState() {
        return nicknameUpdateErrorState;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateNickname(String nickname) {
        repository.isTokenValid(new TokenCallBack() {
            String access_token = repository.getToken().getAccessToken();

            @Override
            public void onSuccess() {
                repository.updateNickname(access_token, nickname, new NicknameCallBack() {
                    @Override
                    public void onSuccess(String nickname) {
                        setNickname(nickname);
                        nicknameUpdateErrorState.setValue(new NicknameUpdateErrorState(null));
                    }

                    @Override
                    public void onFailure(Exception error) {
                        nicknameUpdateErrorState.setValue(new NicknameUpdateErrorState(error));
                    }
                });
            }

            @Override
            public void onFailure() {
                nicknameUpdateErrorState.setValue(
                    new NicknameUpdateErrorState(new UnauthorizedAccessException()));
            }
        });
    }

    public LocalDate getCreatedAt() {
        return repository.getCreatedAt();
    }

    public void logout() {
        repository.logout();
    }
}