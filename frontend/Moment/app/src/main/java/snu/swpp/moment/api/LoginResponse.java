package snu.swpp.moment.api;

import com.google.gson.annotations.SerializedName;


// According to LoginResponse (API) Login response includes user and token
public class LoginResponse {
    @SerializedName("user")
    private User user;

    @SerializedName("token")
    private Token token;

    public User getUser() {
        return user;
    }

    public Token getToken() {
        return token;
    }

    public static class User {
        @SerializedName("username")
        private String username;

        @SerializedName("nickname")
        private String nickname;

        @SerializedName("createdAt")
        private String createdAt;

        public String getUsername() {
            return username;
        }

        public String getNickname() {
            return nickname;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }

    public static class Token {
        @SerializedName("access_token")
        private String accessToken;

        @SerializedName("refresh_token")
        private String refreshToken;

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }
}
