package snu.swpp.moment.data.callback;

import snu.swpp.moment.data.model.LoggedInUserModel;

public interface AuthenticationCallBack {

    void onSuccess(LoggedInUserModel loggedInUser);

    void onFailure(String errorMessage);
}
