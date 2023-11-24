package snu.swpp.moment.ui.main_userinfoview;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.time.LocalDate;
import snu.swpp.moment.data.callback.NicknameCallBack;
import snu.swpp.moment.data.repository.AuthenticationRepository;

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
        repository.updateNickname(nickname, new NicknameCallBack() {
            @Override
            public void onSuccess(String nickname) {
                setNickname(nickname);
            }

            @Override
            public void onFailure(Exception error) {
                nicknameUpdateErrorState.setValue(new NicknameUpdateErrorState(error));
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