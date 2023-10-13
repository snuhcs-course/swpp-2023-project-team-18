package snu.swpp.moment.data;

import snu.swpp.moment.data.model.LoggedInUser;

public interface LoginCallBack {
    void onSuccess(LoggedInUser loggedInUser);
    void onFailure(String errorMessage);
}
