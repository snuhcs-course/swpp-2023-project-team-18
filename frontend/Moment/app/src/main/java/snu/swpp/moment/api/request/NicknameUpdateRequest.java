package snu.swpp.moment.api.request;

import com.google.gson.annotations.SerializedName;

public class NicknameUpdateRequest {

    @SerializedName("nickname")
    private final String nickname;

    public NicknameUpdateRequest(String nickname) {
        this.nickname = nickname;
    }

}
