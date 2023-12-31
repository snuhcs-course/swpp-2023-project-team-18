package snu.swpp.moment.data.model;

import java.time.LocalDateTime;
import snu.swpp.moment.api.response.LoginResponse;
import snu.swpp.moment.api.response.RegisterResponse;
import snu.swpp.moment.utils.TimeConverter;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUserModel {

    private final String username;
    private final String nickName;
    private final String createAt;
    private final String AccessToken;
    private final String RefreshToken;

    public LoggedInUserModel(RegisterResponse.User user, RegisterResponse.Token token) {
        this.username = user.getUsername();
        this.nickName = user.getNickname();
        this.AccessToken = token.getAccessToken();
        this.RefreshToken = token.getRefreshToken();
        this.createAt = LocalDateTime.now().toString();
    }

    public LoggedInUserModel(LoginResponse.User user, LoginResponse.Token token) {
        this.username = user.getUsername();
        this.nickName = user.getNickname();
        this.createAt = TimeConverter.convertUTCToLocalTimeZone(
            user.getCreatedAt().substring(0, 19));
        this.AccessToken = token.getAccessToken();
        this.RefreshToken = token.getRefreshToken();
    }

    public LoggedInUserModel(String username, String nickName, String createAt, String AccessToken,
        String RefreshToken) {
        this.username = username;
        this.nickName = nickName;
        this.createAt = createAt;
        this.AccessToken = AccessToken;
        this.RefreshToken = RefreshToken;
    }

    public String getUsername() {
        return username;
    }

    public String getNickName() {
        return nickName;
    }

    public String getCreateAt() {
        return createAt;
    }

    public String getAccessToken() {
        return AccessToken;
    }

    public String getRefreshToken() {
        return RefreshToken;
    }

}