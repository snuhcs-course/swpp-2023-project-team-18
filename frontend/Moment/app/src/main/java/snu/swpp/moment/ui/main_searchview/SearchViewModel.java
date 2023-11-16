package snu.swpp.moment.ui.main_searchview;

import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import snu.swpp.moment.api.response.SearchContentsResponse;
import snu.swpp.moment.api.response.SearchHashtagsResponse;
import snu.swpp.moment.data.callback.SearchEntriesGetCallBack;
import snu.swpp.moment.data.callback.SearchHashTagCompleteCallBack;
import snu.swpp.moment.data.callback.SearchHashTagGetCallBack;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.repository.AuthenticationRepository;
import snu.swpp.moment.data.repository.SearchRepository;
import snu.swpp.moment.exception.UnauthorizedAccessException;

public class SearchViewModel extends ViewModel {

    private  AuthenticationRepository authenticationRepository;
    private  SearchRepository searchRepository;
    MutableLiveData<SearchType> searchType = new MutableLiveData<>(SearchType.HASHTAG);
    MutableLiveData<HashtagCompletionState> hashtagCompletion = new MutableLiveData<>();
    MutableLiveData<SearchState> searchState = new MutableLiveData<>();



    public SearchViewModel(AuthenticationRepository authenticationRepository,SearchRepository searchRepository) {
        this.authenticationRepository = authenticationRepository;
        this.searchRepository = searchRepository;
    }
    public void setSearchType(SearchType type){
        searchType.setValue(type);
    }
    public void completeHashtag(String query){
        authenticationRepository.isTokenValid(new TokenCallBack() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                searchRepository.getCompleteHashTagList(access_token, query,
                    new SearchHashTagCompleteCallBack() {
                        @Override
                        public void onSuccess(List<String> hashTagList) {
                            hashtagCompletion.setValue(new HashtagCompletionState(hashTagList,null));
                        }

                        @Override
                        public void onFailure(Exception error) {
                            hashtagCompletion.setValue(HashtagCompletionState.withError(error));

                        }
                    });


            }

            @Override
            public void onFailure() {
                hashtagCompletion.setValue(HashtagCompletionState.withError(new UnauthorizedAccessException()));

            }
        });
    }
    public void search(String query){
            authenticationRepository.isTokenValid(new TokenCallBack() {
                @Override
                public void onSuccess() {
                    String access_token = authenticationRepository.getToken().getAccessToken();
                    if(searchType.getValue() == SearchType.HASHTAG){
                        searchRepository.getHashtagSearchList(access_token, query,
                            new SearchHashTagGetCallBack() {
                                @Override
                                public void onSuccess(SearchHashtagsResponse response) {
                                    searchState.setValue(SearchState.fromSearchHashtagsResponse(response));
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    searchState.setValue(SearchState.withError(t));
                                }
                            });
                    }
                    else{
                        searchRepository.getContentSearchList(access_token, query,
                            new SearchEntriesGetCallBack() {
                                @Override
                                public void onSuccess(SearchContentsResponse response) {
                                    Log.d("SearchViewModel", "Content search successful: " + response.toString());
                                    if(response.getSearchentries()!=null) {
                                        searchState.setValue(SearchState.fromSearchContentsResponse(response));
                                        Log.d("SearchViewModel", "Content search size " + response.getSearchentries().size());
                                    }
                                    else{
                                        Log.d("SearchViewModel", "Content search null ");
                                    }

                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    searchState.setValue(SearchState.withError(t));

                                }
                            });

                    }

                }

                @Override
                public void onFailure() {
                    searchState.setValue(SearchState.withError(new UnauthorizedAccessException()));
                }
            });


    }
    public enum  SearchType {
        HASHTAG,CONTENT
    }





}