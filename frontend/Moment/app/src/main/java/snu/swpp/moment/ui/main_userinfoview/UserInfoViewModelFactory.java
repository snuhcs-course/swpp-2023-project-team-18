package snu.swpp.moment.ui.main_userinfoview;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import org.jetbrains.annotations.NotNull;
import snu.swpp.moment.data.repository.AuthenticationRepository;

public class UserInfoViewModelFactory implements ViewModelProvider.Factory {

    private final AuthenticationRepository repository;

    public UserInfoViewModelFactory(AuthenticationRepository repository) {
        this.repository = repository;
    }

    @NotNull
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(UserInfoViewModel.class)) {
            return (T) new UserInfoViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel Class");
    }
}
