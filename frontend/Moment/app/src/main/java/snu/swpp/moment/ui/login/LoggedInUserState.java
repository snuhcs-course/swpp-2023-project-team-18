package snu.swpp.moment.ui.login;

/**
 * Class exposing authenticated user details to the UI.
 */
class LoggedInUserState {

    private final String nickname;
    //... other data fields that may be accessible to the UI

    LoggedInUserState(String nickname) {
        this.nickname = nickname;
    }

    String getNickname() {
        return nickname;
    }
}