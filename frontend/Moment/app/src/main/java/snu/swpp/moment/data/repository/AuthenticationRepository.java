package snu.swpp.moment.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.idling.CountingIdlingResource;
import java.time.LocalDate;
import snu.swpp.moment.data.callback.AuthenticationCallBack;
import snu.swpp.moment.data.callback.NicknameCallBack;
import snu.swpp.moment.data.callback.RefreshCallBack;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.LoggedInUserModel;
import snu.swpp.moment.data.model.TokenModel;
import snu.swpp.moment.data.source.UserLocalDataSource;
import snu.swpp.moment.data.source.UserRemoteDataSource;

/**
 * Class that requests authentication and user information from the remote data source and maintains
 * an in-memory cache of login status and user credentials information.
 */
public class AuthenticationRepository extends BaseRepository<UserRemoteDataSource> {

    private static volatile AuthenticationRepository instance;

    private final UserLocalDataSource localDataSource;
    private final CountingIdlingResource idlingResource;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private LoggedInUserModel user = null;

    // private constructor : singleton access
    private AuthenticationRepository(UserRemoteDataSource remoteDataSource,
        UserLocalDataSource localDataSource) {
        super(remoteDataSource);
        this.localDataSource = localDataSource;
        this.idlingResource = new CountingIdlingResource("authentication");
    }

    public static AuthenticationRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (AuthenticationRepository.class) {
                if (instance == null) {
                    instance = new AuthenticationRepository(new UserRemoteDataSource(),
                        new UserLocalDataSource(context));
                }
            }
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return localDataSource.hasToken();
    }

    public void isTokenValid(TokenCallBack callBack) {
        TokenModel token = localDataSource.getToken();
        idlingResource.increment();
        remoteDataSource.isTokenValid(token.getAccessToken(), new TokenCallBack() {

            @Override
            public void onSuccess() {
                callBack.onSuccess();
                decrementIdlingResource();
            }

            @Override
            public void onFailure() {
                remoteDataSource.isTokenValid(token.getRefreshToken(), new TokenCallBack() {
                    @Override
                    public void onSuccess() {
                        remoteDataSource.refresh(token.getRefreshToken(), new RefreshCallBack() {
                            @Override
                            public void onSuccess(String access) {
                                localDataSource.saveToken(access);
                                callBack.onSuccess();
                                decrementIdlingResource();
                            }

                            @Override
                            public void onFailure() {
                                callBack.onFailure();
                                decrementIdlingResource();
                            }
                        });
                    }

                    @Override
                    public void onFailure() {
                        callBack.onFailure();
                        decrementIdlingResource();
                    }
                });
            }
        });
    }

    public void logout() {
        localDataSource.logout();
    }

    private void setLoggedInUser(LoggedInUserModel user) {
        this.user = user;
        localDataSource.saveUser(user);

        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    public void login(String username, String password, AuthenticationCallBack loginCallBack) {
        idlingResource.increment();
        remoteDataSource.login(username, password, new AuthenticationCallBack() {
            @Override
            public void onSuccess(LoggedInUserModel loggedInUser) {
                Log.d("AuthenticationRepository", "login success");
                setLoggedInUser(loggedInUser);
                loginCallBack.onSuccess(loggedInUser);
                decrementIdlingResource();
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d("AuthenticationRepository", "login failure");
                loginCallBack.onFailure(errorMessage);
                decrementIdlingResource();
            }
        });
    }

    public void register(String username, String password, String nickname,
        AuthenticationCallBack registerCallBack) {
        remoteDataSource.register(username, password, nickname, new AuthenticationCallBack() {
            @Override
            public void onSuccess(LoggedInUserModel loggedInUser) {
                setLoggedInUser(loggedInUser);
                registerCallBack.onSuccess(loggedInUser);
            }

            @Override
            public void onFailure(String errorMessage) {
                registerCallBack.onFailure(errorMessage);
            }
        });
    }

    public String getNickname() {
        return localDataSource.getNickname();
    }


    public void updateNickname(String nickname, NicknameCallBack callback) {
        String access_token = localDataSource.getToken().getAccessToken();
        remoteDataSource.updateNickname(access_token, nickname, new NicknameCallBack() {
            @Override
            public void onSuccess(String nickname) {
                localDataSource.saveNickname(nickname);
                callback.onSuccess(nickname);
            }

            @Override
            public void onFailure(Exception error) {
                callback.onFailure(error);
            }
        });
    }

    public TokenModel getToken() {
        return localDataSource.getToken();
    }

    public LocalDate getCreatedAt() {
        return localDataSource.getCreatedAt();
    }

    public IdlingResource getIdlingResource() {
        return idlingResource;
    }

    private void decrementIdlingResource() {
        if (!idlingResource.isIdleNow()) {
            idlingResource.decrement();
        }
    }
}