package snu.swpp.moment.data.source;

import snu.swpp.moment.api.RetrofitClient;
import snu.swpp.moment.api.ServiceApi;

public abstract class BaseRemoteDataSource {

    protected ServiceApi service;

    public BaseRemoteDataSource() {
        service = RetrofitClient.getClient().create(ServiceApi.class);
    }
}
