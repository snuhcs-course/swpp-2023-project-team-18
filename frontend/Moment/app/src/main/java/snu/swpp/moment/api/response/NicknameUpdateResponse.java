package snu.swpp.moment.api.response;

import com.google.gson.annotations.SerializedName;

public class NicknameUpdateResponse {

    @SerializedName("nickname")
    private String nickname;

    public String getNickname() {
        return nickname;
    }
}
