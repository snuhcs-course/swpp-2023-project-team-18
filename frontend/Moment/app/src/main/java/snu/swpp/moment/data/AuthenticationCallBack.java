package snu.swpp.moment.data;

import snu.swpp.moment.data.model.LoggedInUser;

public interface AuthenticationCallBack {
    void onLoginSuccess(LoggedInUser loggedInUser);
    void onLoginFailure(String errorMessage);
}
