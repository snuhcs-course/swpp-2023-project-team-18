package snu.swpp.moment.ui.register;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import java.io.IOException;
import java.security.GeneralSecurityException;
import snu.swpp.moment.data.repository.AuthenticationRepository;

/**
 * ViewModel provider factory to instantiate LoginViewModel. Required given LoginViewModel has a
 * non-empty constructor
 */
public class RegisterViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public RegisterViewModelFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RegisterViewModel.class)) {
            return (T) new RegisterViewModel(AuthenticationRepository.getInstance(context));
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}