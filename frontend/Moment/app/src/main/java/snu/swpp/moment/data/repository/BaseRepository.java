package snu.swpp.moment.data.repository;

import snu.swpp.moment.data.source.BaseRemoteDataSource;

abstract public class BaseRepository<T extends BaseRemoteDataSource> implements
    Repository {

    protected final T remoteDataSource;

    public BaseRepository(T remoteDataSource) {
        this.remoteDataSource = remoteDataSource;
    }
}
