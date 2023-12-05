package snu.swpp.moment.ui.register;

/**
 * Class exposing authenticated user details to the UI.
 */
class RegisterUserState {

    private final String nickname;
    //... other data fields that may be accessible to the UI

    RegisterUserState(String nickname) {
        this.nickname = nickname;
    }

    String getNickname() {
        return nickname;
    }
}