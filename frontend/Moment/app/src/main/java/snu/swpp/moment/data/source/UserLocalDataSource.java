package snu.swpp.moment.data.source;
// To use SharedPreferences, import Context

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import java.io.IOException;
import java.security.GeneralSecurityException;
import snu.swpp.moment.data.model.LoggedInUser;
import snu.swpp.moment.data.model.Token;

public class UserLocalDataSource {

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    private final String DEFAULT_STRING = "";

    public UserLocalDataSource(Context context) throws GeneralSecurityException, IOException {
        String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        sharedPreferences = EncryptedSharedPreferences.create(
            "secret_tokens",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
        editor = sharedPreferences.edit(); // editor가 sharedPreference를 참조
        // read : shared prefernce, write : editor  -> secret_tokens에
    }

    public void saveUser(LoggedInUser user) { // 이거 있으면 회원가입, 로그인 다 access token으로 접근해야하니까
        editor.putString("nickname", user.getNickName());
        editor.putString("access_token", user.getAccessToken());
        editor.putString("refresh_token", user.getRefreshToken());
        editor.putString("created_at", user.getCreateAt()); // YYYY-MM-DDTHH:SS:...
        editor.apply(); // 이거 해야 적용됨
        // username은 오는데 저장은 따로 아직 안했음 (굳이?)
    }

    public void saveToken(String token) {
        editor.putString("access_token", token);
        editor.apply();
    }

    public Token getToken() {
        String accessToken = sharedPreferences.getString("access_token", DEFAULT_STRING);
        String refreshToken = sharedPreferences.getString("refresh_token", DEFAULT_STRING);
        return new Token(accessToken, refreshToken);
    }

    public boolean hasToken() {
        return sharedPreferences.contains("access_token") && sharedPreferences.contains(
            "refresh_token");
    }

    public String getCreatedAt() {
        return sharedPreferences.getString("created_at", DEFAULT_STRING);
    }

    public void logout() {
        editor.remove("nickname");
        editor.remove("access_token");
        editor.remove("refresh_token");
        editor.apply();
    }
}
