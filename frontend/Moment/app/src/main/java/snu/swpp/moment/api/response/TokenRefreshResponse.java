package snu.swpp.moment.api.response;

import com.google.gson.annotations.SerializedName;

public class TokenRefreshResponse {

    @SerializedName("access")
    private String access;

    public TokenRefreshResponse(String access) {
        this.access = access;
    }

    public String getAccess() {
        return access;
    }
}
