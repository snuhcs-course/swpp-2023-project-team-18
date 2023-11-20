package snu.swpp.moment.data.factory;

import snu.swpp.moment.data.repository.MomentRepository;
import snu.swpp.moment.data.source.MomentRemoteDataSource;

public class MomentRepositoryFactory implements RepositoryFactory {

    private MomentRemoteDataSource dataSource;
    private MomentRepository repository;

    public MomentRemoteDataSource getRemoteDataSource() {
        if (dataSource == null) {
            dataSource = new MomentRemoteDataSource();
        }
        return dataSource;
    }

    @Override
    public MomentRepository getRepository() {
        if (repository == null) {
            repository = new MomentRepository(getRemoteDataSource());
        }
        return repository;
    }
}
