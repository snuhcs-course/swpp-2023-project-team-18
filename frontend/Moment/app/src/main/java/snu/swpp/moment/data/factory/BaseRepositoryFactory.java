package snu.swpp.moment.data.factory;

import snu.swpp.moment.data.repository.Repository;

public interface BaseRepositoryFactory {

    Repository getRepository();
}