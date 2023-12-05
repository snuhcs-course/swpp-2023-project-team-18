package snu.swpp.moment.ui.login;

import androidx.annotation.Nullable;

/**
 * Authentication result : success (user details) or error message.
 */
class LoginResultState {

    @Nullable
    private LoggedInUserState success;
    @Nullable
    private Integer error;

    LoginResultState(@Nullable Integer error) {
        this.error = error;
    }

    LoginResultState(@Nullable LoggedInUserState success) {
        this.success = success;
    }

    @Nullable
    LoggedInUserState getSuccess() {
        return success;
    }

    @Nullable
    Integer getError() {
        return error;
    }
}