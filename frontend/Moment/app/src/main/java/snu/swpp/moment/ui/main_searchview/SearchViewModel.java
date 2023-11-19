package snu.swpp.moment.ui.main_searchview;

import android.util.Log;

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
    MutableLiveData<HashtagCompletionState> hashtagCompletionState = new MutableLiveData<>();
    MutableLiveData<SearchState> searchState = new MutableLiveData<>();
    MutableLiveData<String> selectedHashtag = new MutableLiveData<>("");



    public SearchViewModel(AuthenticationRepository authenticationRepository,SearchRepository searchRepository) {
        this.authenticationRepository = authenticationRepository;
        this.searchRepository = searchRepository;
    }
    public void setSearchType(SearchType type){
        searchType.setValue(type);
    }
    public void completeHashtag(String query){
        Log.d("searchViewModel", "send API");
        authenticationRepository.isTokenValid(new TokenCallBack() {
            @Override
            public void onSuccess() {
                String access_token = authenticationRepository.getToken().getAccessToken();
                searchRepository.getCompleteHashTagList(access_token, query,
                    new SearchHashTagCompleteCallBack() {
                        @Override
                        public void onSuccess(List<String> hashTagList) {
                            hashtagCompletionState.setValue(new HashtagCompletionState(hashTagList,null));
                        }

                        @Override
                        public void onFailure(Exception error) {
                            hashtagCompletionState.setValue(HashtagCompletionState.withError(error));

                        }
                    });


            }

            @Override
            public void onFailure() {
                hashtagCompletionState.setValue(HashtagCompletionState.withError(new UnauthorizedAccessException()));

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
                                    Log.d("SearchViewModel", "Hashtag search successful: " + response.getSearchentries());
                                    selectedHashtag.setValue(query);
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
                                    Log.d("SearchViewModel", "Content search successful: " + response.getSearchentries());

                                    if(response.getSearchentries()!=null) {
                                        searchState.setValue(SearchState.fromSearchContentsResponse(response));
                                    }
                                    else{
                                    //    Log.d("SearchViewModel", "Content search null ");
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