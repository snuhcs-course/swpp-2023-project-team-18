package snu.swpp.moment.api.request;

import com.google.gson.annotations.SerializedName;

public class TokenVerifyRequest {

    @SerializedName("token")
    private final String token;

    public TokenVerifyRequest(String token) {
        this.token = token;
    }
}
