package snu.swpp.moment.data.factory;

import snu.swpp.moment.data.repository.SearchRepository;
import snu.swpp.moment.data.source.SearchRemoteDataSource;

public class SearchRepositoryFactory implements BaseRepositoryFactory {

    private SearchRemoteDataSource dataSource;
    private SearchRepository repository;

    public SearchRemoteDataSource getRemoteDataSource() {
        if (dataSource == null) {
            dataSource = new SearchRemoteDataSource();
        }
        return dataSource;
    }

    @Override
    public SearchRepository getRepository() {
        if (repository == null) {
            repository = new SearchRepository(getRemoteDataSource());
        }
        return repository;
    }
}
