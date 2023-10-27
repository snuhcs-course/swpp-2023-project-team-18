package snu.swpp.moment.ui.main_writeview;

import androidx.lifecycle.ViewModel;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;
import snu.swpp.moment.data.repository.StoryRepository;

public class DailyViewModel extends ViewModel {

    private final AuthenticationRepository authenticationRepository;
    private final MomentRepository momentRepository;
    private final StoryRepository storyRepository;

    public DailyViewModel(
        AuthenticationRepository authenticationRepository,
        MomentRepository momentRepository,
        StoryRepository storyRepository
    ) {
        this.authenticationRepository = authenticationRepository;
        this.momentRepository = momentRepository;
        this.storyRepository = storyRepository;
    }



}
