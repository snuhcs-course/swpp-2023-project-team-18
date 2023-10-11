package snu.swpp.moment.api;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("user")
    private User user;

    @SerializedName("token")
    private Token token;

    // Getters and setters
    public User getUser(){
        return this.user;
    }

    public Token getToken(){
        return this.token;
    }

    public static class User {
        @SerializedName("username")
        private String username;

        @SerializedName("nickname")
        private String nickname;

        @SerializedName("createdAt")
        private String createdAt;

        // Getters and setters
        public String getUsername(){
            return this.username;
        }

        public String getNickname(){
            return this.nickname;
        }

        public String getCreatedAt(){
            return this.createdAt;
        }
    }

    public static class Token {
        @SerializedName("access_token")
        private String accessToken;

        @SerializedName("refresh_token")
        private String refreshToken;

        // Getters and setters
        public String getAccessToken(){
            return this.accessToken;
        }
        public String getRefreshToken(){
            return this.refreshToken;
        }
    }

}
