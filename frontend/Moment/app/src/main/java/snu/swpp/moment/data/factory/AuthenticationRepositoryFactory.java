package snu.swpp.moment.data.factory;

import android.content.Context;
import snu.swpp.moment.data.repository.AuthenticationRepository;

public class AuthenticationRepositoryFactory implements BaseRepositoryFactory {

    private final Context context;
    private AuthenticationRepository repository;

    public AuthenticationRepositoryFactory(Context context) {
        super();
        this.context = context;
    }

    @Override
    public AuthenticationRepository getRepository() {
        if (repository == null) {
            repository = AuthenticationRepository.getInstance(context);
        }
        return repository;
    }
}
