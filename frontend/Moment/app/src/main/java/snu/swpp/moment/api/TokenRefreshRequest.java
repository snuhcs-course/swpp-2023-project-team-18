package snu.swpp.moment.api;

import com.google.gson.annotations.SerializedName;

public class TokenRefreshRequest {
    @SerializedName("refresh")
    private String refresh;

    public TokenRefreshRequest(String refresh) {
        this.refresh = refresh;
    }
}
