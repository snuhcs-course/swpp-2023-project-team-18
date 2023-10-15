package snu.swpp.moment.ui.login;

/**
 * Class exposing authenticated user details to the UI.
 */
class LoggedInUserView {
    private String nickname;
    //... other data fields that may be accessible to the UI

    LoggedInUserView(String nickname) {
        this.nickname = nickname;
    }

    String getDisplayName() {
        return nickname;
    }
}