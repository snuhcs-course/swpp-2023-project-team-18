package snu.swpp.moment.data.repository;

import java.util.List;
import snu.swpp.moment.api.response.SearchContentsResponse;
import snu.swpp.moment.api.response.SearchHashtagsResponse;
import snu.swpp.moment.data.callback.SearchEntriesGetCallBack;
import snu.swpp.moment.data.callback.SearchHashTagCompleteCallBack;
import snu.swpp.moment.data.callback.SearchHashTagGetCallBack;
import snu.swpp.moment.data.source.SearchRemoteDataSource;

public class SearchRepository extends BaseRepository<SearchRemoteDataSource> {


    public SearchRepository(SearchRemoteDataSource remoteDataSource) {
        super(remoteDataSource);
    }

    public void getCompleteHashTagList(String access_token, String query,
        SearchHashTagCompleteCallBack callback) {
        remoteDataSource.getCompleteHashTagList(access_token, query,
            new SearchHashTagCompleteCallBack() {
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

    public void getContentSearchList(String access_token, String query,
        SearchEntriesGetCallBack callback) {
        remoteDataSource.getContentSearchList(access_token, query,
            new SearchEntriesGetCallBack() {
                @Override
                public void onSuccess(SearchContentsResponse response) {
                    callback.onSuccess(response);
                }

                @Override
                public void onFailure(Throwable t) {
                    callback.onFailure(t);
                }
            });
    }

    public void getHashtagSearchList(String access_token, String query,
        SearchHashTagGetCallBack callback) {
        remoteDataSource.getHashtagSearchList(access_token, query,
            new SearchHashTagGetCallBack() {
                @Override
                public void onSuccess(SearchHashtagsResponse response) {
                    callback.onSuccess(response);
                }

                @Override
                public void onFailure(Throwable t) {
                    callback.onFailure(t);
                }
            });
    }
}
