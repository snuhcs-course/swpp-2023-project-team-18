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

    private UserRemoteDataSource remoteDataSource;
    private UserLocalDataSource localDataSource;

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
        //System.out.println("#Debug AuthenticationRepository :: setLoggedInLuser");
        localDataSource.saveUser(user);

        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    public void login(String username, String password, AuthenticationCallBack loginCallBack) {
        //System.out.println("#Debug from LoginRepository || username : " + username + "password : " + password);
        // handle login
        remoteDataSource.login(username, password, new AuthenticationCallBack() {
            @Override
            public void onSuccess(LoggedInUser loggedInUser) {
                setLoggedInUser(loggedInUser);
                //System.out.println("#DEBUG Login Success");
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
        //System.out.println("#Debug from LoginRepository || username : " + username + "password : " + password);
        // handle login
        remoteDataSource.register(username, password, nickname, new AuthenticationCallBack() {
            @Override
            public void onSuccess(LoggedInUser loggedInUser) {
                setLoggedInUser(loggedInUser);
                System.out.println("#DEBUG Login Success");
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
}