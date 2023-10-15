package snu.swpp.moment.api;

import com.google.gson.annotations.SerializedName;

import retrofit2.http.POST;

public class RegisterRequest {
    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    @SerializedName("nickname")
    private String nickname;

    public RegisterRequest(String username, String password, String nickname){
        this.username = username;
        this.password = password;
        this.nickname = nickname;
    }
}
