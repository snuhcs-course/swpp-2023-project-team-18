package snu.swpp.moment.ui.main_writeview.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;

public class DailyViewModelFactory implements ViewModelProvider.Factory {

    private final AuthenticationRepository authenticationRepository;
    private final MomentRepository momentRepository;
    private final GetStoryUseCase getStoryUseCase;
    private final SaveScoreUseCase saveScoreUseCase;

    public DailyViewModelFactory(
        AuthenticationRepository authenticationRepository,
        MomentRepository momentRepository,
        GetStoryUseCase getStoryUseCase,
        SaveScoreUseCase saveScoreUseCase
    ) {
        this.authenticationRepository = authenticationRepository;
        this.momentRepository = momentRepository;
        this.getStoryUseCase = getStoryUseCase;
        this.saveScoreUseCase = saveScoreUseCase;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DailyViewModel.class)) {
            return (T) new DailyViewModel(
                authenticationRepository,
                momentRepository,
                getStoryUseCase,
                saveScoreUseCase
            );
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
