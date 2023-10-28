package snu.swpp.moment.ui.main_writeview;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;
import snu.swpp.moment.data.repository.StoryRepository;

public class TodayViewModelFactory implements ViewModelProvider.Factory {

    private final AuthenticationRepository authenticationRepository;
    private final MomentRepository momentRepository;
    private final StoryRepository storyRepository;

    public TodayViewModelFactory(
        AuthenticationRepository authenticationRepository,
        MomentRepository momentRepository,
        StoryRepository storyRepository
    ) {
        this.authenticationRepository = authenticationRepository;
        this.momentRepository = momentRepository;
        this.storyRepository = storyRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TodayViewModel.class)) {
            return (T) new TodayViewModel(authenticationRepository, momentRepository,
                storyRepository);
        } else {
            System.out.println("#DEBUG: WriteViewModelFactory not working");
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
