package snu.swpp.moment.ui.main_searchview;

import java.util.List;
import snu.swpp.moment.api.response.SearchHashTagGetCompleteResponse;

public class HashtagCompletionState {
    List<String> hashtags;
    Throwable error;

    public HashtagCompletionState(List<String> hashtags, Throwable error) {
        this.hashtags = hashtags;
        this.error = error;
    }

    public static HashtagCompletionState withError(Throwable e){
        return new HashtagCompletionState(null,e);
    }
    public static HashtagCompletionState fromSearchHashtagGetCompleteResponse(SearchHashTagGetCompleteResponse response){
        return new HashtagCompletionState(response.getHashtagList(),null);
    }
}
