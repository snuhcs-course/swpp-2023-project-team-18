package snu.swpp.moment.ui.main_writeview.viewmodel;

import android.content.Context;
import androidx.annotation.NonNull;
import snu.swpp.moment.data.factory.AuthenticationRepositoryFactory;
import snu.swpp.moment.data.factory.MomentRepositoryFactory;
import snu.swpp.moment.data.factory.StoryRepositoryFactory;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.MomentRepository;
import snu.swpp.moment.data.repository.StoryRepository;

public class WritePageDataUnitFactory {

    private final Context context;
    private GetStoryUseCase getStoryUseCase;
    private SaveScoreUseCase saveScoreUseCase;
    private AuthenticationRepositoryFactory authenticationRepositoryFactory;
    private MomentRepositoryFactory momentRepositoryFactory;
    private StoryRepositoryFactory storyRepositoryFactory;

    public WritePageDataUnitFactory(@NonNull Context context) {
        this.context = context;
    }

    public @NonNull GetStoryUseCase getStoryUseCase() throws RuntimeException {
        if (getStoryUseCase == null) {
            getStoryUseCase = new GetStoryUseCase(authenticationRepository(), storyRepository());
        }
        return getStoryUseCase;
    }

    public @NonNull SaveScoreUseCase saveScoreUseCase() throws RuntimeException {
        if (saveScoreUseCase == null) {
            saveScoreUseCase = new SaveScoreUseCase(authenticationRepository(), storyRepository());
        }
        return saveScoreUseCase;
    }

    public @NonNull AuthenticationRepository authenticationRepository() throws RuntimeException {
        if (authenticationRepositoryFactory == null) {
            authenticationRepositoryFactory = new AuthenticationRepositoryFactory(context);
        }
        return authenticationRepositoryFactory.getRepository();
    }

    public @NonNull MomentRepository momentRepository() {
        if (momentRepositoryFactory == null) {
            momentRepositoryFactory = new MomentRepositoryFactory();
        }
        return momentRepositoryFactory.getRepository();
    }

    public @NonNull StoryRepository storyRepository() {
        if (storyRepositoryFactory == null) {
            storyRepositoryFactory = new StoryRepositoryFactory();
        }
        return storyRepositoryFactory.getRepository();
    }
}
