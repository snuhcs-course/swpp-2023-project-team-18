package snu.swpp.moment.data.source;
// To use SharedPreferences, import Context

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import snu.swpp.moment.data.model.LoggedInUserModel;
import snu.swpp.moment.data.model.TokenModel;
import snu.swpp.moment.utils.TimeConverter;

public class UserLocalDataSource {

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    private final String DEFAULT_STRING = "";

    public UserLocalDataSource(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                "secret_tokens",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Fail to initialize shared preference", e);
        }
        editor = sharedPreferences.edit(); // editor가 sharedPreference를 참조
        // read : shared prefernce, write : editor  -> secret_tokens에
    }

    public void saveUser(LoggedInUserModel user) { // 이거 있으면 회원가입, 로그인 다 access token으로 접근해야하니까
        editor.putString("nickname", user.getNickName());
        editor.putString("access_token", user.getAccessToken());
        editor.putString("refresh_token", user.getRefreshToken());
        editor.putString("created_at", user.getCreateAt()); // YYYY-MM-DDTHH:SS:...
        editor.apply(); // 이거 해야 적용됨
        // username은 오는데 저장은 따로 아직 안했음 (굳이?)
    }

    public void saveNickname(String nickname) {
        editor.putString("nickname", nickname);
        editor.apply();
    }

    public String getNickname() {
        return sharedPreferences.getString("nickname", DEFAULT_STRING);
    }

    public void saveToken(String token) {
        editor.putString("access_token", token);
        editor.apply();
    }

    public TokenModel getToken() {
        String accessToken = sharedPreferences.getString("access_token", DEFAULT_STRING);
        String refreshToken = sharedPreferences.getString("refresh_token", DEFAULT_STRING);
        return new TokenModel(accessToken, refreshToken);
    }

    public boolean hasToken() {
        return sharedPreferences.contains("access_token") && sharedPreferences.contains(
            "refresh_token");
    }

    public LocalDate getCreatedAt() {
        String dateTimeInString = sharedPreferences.getString("created_at", DEFAULT_STRING)
            .substring(0, 19); // 초 단위까지만 parsing;
        if (dateTimeInString.isBlank()) {
            return TimeConverter.getToday();
        }
        return TimeConverter.adjustToServiceDate(LocalDateTime.parse(dateTimeInString));
    }

    public void logout() {
        editor.remove("nickname");
        editor.remove("access_token");
        editor.remove("refresh_token");
        editor.apply();
    }
}
