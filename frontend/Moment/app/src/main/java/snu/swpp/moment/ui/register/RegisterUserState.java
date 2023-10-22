package snu.swpp.moment.ui.register;

/**
 * Class exposing authenticated user details to the UI.
 */
class RegisterUserState {

    private String nickname;
    //... other data fields that may be accessible to the UI

    RegisterUserState(String displayName) {
        this.nickname = nickname;
    }

    String getDisplayName() {
        return nickname;
    }
}