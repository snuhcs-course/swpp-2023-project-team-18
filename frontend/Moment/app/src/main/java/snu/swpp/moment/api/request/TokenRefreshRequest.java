package snu.swpp.moment.api.request;

import com.google.gson.annotations.SerializedName;

public class TokenRefreshRequest {

    @SerializedName("refresh")
    private final String refresh;

    public TokenRefreshRequest(String refresh) {
        this.refresh = refresh;
    }
}
