package snu.swpp.moment.ui.main_writeview;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;

public class WriteViewModelFactory implements ViewModelProvider.Factory {

    private AuthenticationRepository authenticationRepository;
    private MomentRepository momentRepository;

    public WriteViewModelFactory(
        AuthenticationRepository authenticationRepository,
        MomentRepository momentRepository
    ) {
        this.authenticationRepository = authenticationRepository;
        this.momentRepository = momentRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(WriteViewModel.class)) {
            return (T) new WriteViewModel(authenticationRepository, momentRepository);
        } else {
            System.out.println("#DEBUG: WriteViewModelFactory not working");
            throw new IllegalArgumentException("Unkown ViewModel class");
        }
    }
}
