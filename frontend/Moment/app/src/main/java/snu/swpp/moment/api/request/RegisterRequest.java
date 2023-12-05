package snu.swpp.moment.api.request;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {

    @SerializedName("username")
    private final String username;

    @SerializedName("password")
    private final String password;

    @SerializedName("nickname")
    private final String nickname;

    public RegisterRequest(String username, String password, String nickname) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
    }
}
