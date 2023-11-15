package snu.swpp.moment.api.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SearchHashTagGetCompleteResponse {

    @SerializedName("hashtags")
    private List<String> hashtagList;

    public SearchHashTagGetCompleteResponse(List<String> hashtags) {
        this.hashtagList = hashtags;
    }

    public List<String> getHashtagList() {
        return hashtagList;
    }

}
