package snu.swpp.moment.ui.register;

/**
 * Class exposing authenticated user details to the UI.
 */
class RegisterUserView {

    private String displayName;
    //... other data fields that may be accessible to the UI

    RegisterUserView(String displayName) {
        this.displayName = displayName;
    }

    String getDisplayName() {
        return displayName;
    }
}