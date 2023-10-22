package snu.swpp.moment.ui.register;

import androidx.annotation.Nullable;

/**
 * Authentication result : success (user details) or error message.
 */
class RegisterResultState {

    @Nullable
    private RegisterUserState success;
    @Nullable
    private Integer error;

    RegisterResultState(@Nullable Integer error) {
        this.error = error;
    }

    RegisterResultState(@Nullable RegisterUserState success) {
        this.success = success;
    }

    @Nullable
    RegisterUserState getSuccess() {
        return success;
    }

    @Nullable
    Integer getError() {
        return error;
    }
}