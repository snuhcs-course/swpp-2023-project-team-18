package snu.swpp.moment.api;

import com.google.gson.annotations.SerializedName;

public class TokenVerifyRequest {

    @SerializedName("token")
    private String token;

    public TokenVerifyRequest(String token) {
        this.token = token;
    }
}
