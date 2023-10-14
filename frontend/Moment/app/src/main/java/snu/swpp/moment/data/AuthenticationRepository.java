package snu.swpp.moment.data;

import android.content.Context;

import java.io.IOException;
import java.security.GeneralSecurityException;

import snu.swpp.moment.api.LoginResponse;
import snu.swpp.moment.data.model.LoggedInUser;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class AuthenticationRepository {

    private static volatile AuthenticationRepository instance;

    private UserRemoteDataSource remoteDataSource;
    private UserLocalDataSource localDataSource;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private LoggedInUser user = null;

    // private constructor : singleton access
    private AuthenticationRepository(UserRemoteDataSource remoteDataSource, UserLocalDataSource localDataSource) {
        this.remoteDataSource = remoteDataSource;
        this.localDataSource = localDataSource;
    }

    public static AuthenticationRepository getInstance(Context context) throws GeneralSecurityException, IOException {
        if (instance == null) {
            instance = new AuthenticationRepository(new UserRemoteDataSource(), new UserLocalDataSource(context));
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public void logout() {
        user = null;
        remoteDataSource.logout();
    }

    private void setLoggedInUser(LoggedInUser user) {
        this.user = user;
        System.out.println("#Debug AuthenticationRepository :: setLoggedInLuser");
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
                System.out.println("#DEBUG Login Success");
                loginCallBack.onSuccess(loggedInUser);
            }
            @Override
            public void onFailure(String errorMessage) {
                loginCallBack.onFailure(errorMessage);
            }
        });
    }
}