package snu.swpp.moment.data.factory;

import snu.swpp.moment.data.repository.StoryRepository;
import snu.swpp.moment.data.source.StoryRemoteDataSource;

public class StoryRepositoryFactory implements RepositoryFactory {

    private StoryRemoteDataSource dataSource;
    private StoryRepository repository;

    public StoryRemoteDataSource getRemoteDataSource() {
        if (dataSource == null) {
            dataSource = new StoryRemoteDataSource();
        }
        return dataSource;
    }

    @Override
    public StoryRepository getRepository() {
        if (repository == null) {
            repository = new StoryRepository(getRemoteDataSource());
        }
        return repository;
    }
}
