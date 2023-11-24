package snu.swpp.moment.data.factory;

import snu.swpp.moment.data.repository.NudgeRepository;
import snu.swpp.moment.data.repository.SearchRepository;
import snu.swpp.moment.data.source.NudgeRemoteDataSource;
import snu.swpp.moment.data.source.SearchRemoteDataSource;

public class NudgeRepositoryFactory implements RepositoryFactory {

    private NudgeRemoteDataSource dataSource;
    private NudgeRepository repository;

    public NudgeRemoteDataSource getRemoteDataSource() {
        if (dataSource == null) {
            dataSource = new NudgeRemoteDataSource();
        }
        return dataSource;
    }

    @Override
    public NudgeRepository getRepository() {
        if (repository == null) {
            repository = new NudgeRepository(getRemoteDataSource());
        }
        return repository;
    }
}
