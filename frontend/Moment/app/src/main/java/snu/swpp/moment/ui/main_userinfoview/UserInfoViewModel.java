package snu.swpp.moment.ui.main_userinfoview;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;

public class UserInfoViewModel extends ViewModel {

    private String nickname;
    private final AuthenticationRepository repository;

    public UserInfoViewModel(AuthenticationRepository repository) {
        this.repository = repository;
        this.nickname = "닉네임";
    }
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void logout() {
        repository.logout();
    }
}