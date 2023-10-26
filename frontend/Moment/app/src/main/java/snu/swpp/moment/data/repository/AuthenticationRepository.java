package snu.swpp.moment.data.repository;

import android.content.Context;
import java.io.IOException;
import java.security.GeneralSecurityException;
import snu.swpp.moment.data.callback.AuthenticationCallBack;
import snu.swpp.moment.data.callback.RefreshCallBack;
import snu.swpp.moment.data.callback.TokenCallBack;
import snu.swpp.moment.data.model.LoggedInUser;
import snu.swpp.moment.data.model.Token;
import snu.swpp.moment.data.source.UserLocalDataSource;
import snu.swpp.moment.data.source.UserRemoteDataSource;

/**
 * Class that requests authentication and user information from the remote data source and maintains
 * an in-memory cache of login status and user credentials information.
 */
public class AuthenticationRepository {

    private static volatile AuthenticationRepository instance;

    private final UserRemoteDataSource remoteDataSource;
    private final UserLocalDataSource localDataSource;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private LoggedInUser user = null;

    // private constructor : singleton access
    private AuthenticationRepository(UserRemoteDataSource remoteDataSource,
        UserLocalDataSource localDataSource) {
        this.remoteDataSource = remoteDataSource;
        this.localDataSource = localDataSource;
    }

    public static AuthenticationRepository getInstance(Context context)
        throws GeneralSecurityException, IOException {
        if (instance == null) {
            instance = new AuthenticationRepository(new UserRemoteDataSource(),
                new UserLocalDataSource(context));
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return localDataSource.hasToken();
    }

    public void isTokenValid(TokenCallBack callBack) {
        Token token = localDataSource.getToken();
        remoteDataSource.isTokenValid(token.getAccessToken(), new TokenCallBack() {

            @Override
            public void onSuccess() {
                callBack.onSuccess();
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
                            }

                            @Override
                            public void onFailure() {
                                callBack.onFailure();
                            }
                        });
                    }

                    @Override
                    public void onFailure() {
                        callBack.onFailure();
                    }
                });
            }
        });
    }

    public void logout() {
        localDataSource.logout();
    }

    private void setLoggedInUser(LoggedInUser user) {
        this.user = user;
        localDataSource.saveUser(user);

        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    public void login(String username, String password, AuthenticationCallBack loginCallBack) {
        remoteDataSource.login(username, password, new AuthenticationCallBack() {
            @Override
            public void onSuccess(LoggedInUser loggedInUser) {
                setLoggedInUser(loggedInUser);
                loginCallBack.onSuccess(loggedInUser);
            }

            @Override
            public void onFailure(String errorMessage) {
                loginCallBack.onFailure(errorMessage);
            }
        });
    }


    public void register(String username, String password, String nickname,
        AuthenticationCallBack registerCallBack) {
        remoteDataSource.register(username, password, nickname, new AuthenticationCallBack() {
            @Override
            public void onSuccess(LoggedInUser loggedInUser) {
                setLoggedInUser(loggedInUser);
                registerCallBack.onSuccess(loggedInUser);
            }

            @Override
            public void onFailure(String errorMessage) {
                registerCallBack.onFailure(errorMessage);
            }
        });
    }

    public Token getToken() {
        return localDataSource.getToken();
    }

    public String getCreatedAt() {
        return localDataSource.getCreatedAt();
    }
}