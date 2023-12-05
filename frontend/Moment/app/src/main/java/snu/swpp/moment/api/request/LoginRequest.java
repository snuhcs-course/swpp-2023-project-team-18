package snu.swpp.moment.api.request;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {

    @SerializedName("username")
    private final String username;
    @SerializedName("password")
    private final String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
