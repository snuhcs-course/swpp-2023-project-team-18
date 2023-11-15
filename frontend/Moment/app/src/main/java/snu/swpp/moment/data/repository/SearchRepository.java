package snu.swpp.moment.data.repository;

import java.util.List;

import snu.swpp.moment.data.callback.SearchHashTagCompleteCallBack;
import snu.swpp.moment.data.source.SearchRemoteDataSource;

public class SearchRepository {

    private final SearchRemoteDataSource searchRemoteDataSource;

    public SearchRepository(SearchRemoteDataSource remoteDataSource) {
        this.searchRemoteDataSource = remoteDataSource;
    }

    public void getCompleteHashTagList(String access_token, String query, SearchHashTagCompleteCallBack callback) {
        searchRemoteDataSource.getCompleteHashTagList(access_token, query, new SearchHashTagCompleteCallBack() {
            @Override
            public void onSuccess(List<String> hashTagList) {
                callback.onSuccess(hashTagList);
            }

            @Override
            public void onFailure(Exception error) {
                callback.onFailure(error);
            }
        });
    }
}