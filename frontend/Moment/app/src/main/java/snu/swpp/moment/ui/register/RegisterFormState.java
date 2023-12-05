package snu.swpp.moment.ui.register;

import androidx.annotation.Nullable;

/**
 * Data validation state of the login form.
 */
class RegisterFormState {

    @Nullable
    private final Integer usernameError;
    @Nullable
    private final Integer passwordError;
    @Nullable
    private final Integer passwordDiffError;
    private final boolean isDataValid;

    RegisterFormState(@Nullable Integer usernameError, @Nullable Integer passwordError,
        @Nullable Integer passwordDiffError) {
        this.usernameError = usernameError;
        this.passwordError = passwordError;
        this.passwordDiffError = passwordDiffError;
        this.isDataValid = false;
    }

    RegisterFormState(boolean isDataValid) {
        this.usernameError = null;
        this.passwordError = null;
        this.passwordDiffError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    Integer getUsernameError() {
        return usernameError;
    }

    @Nullable
    Integer getPasswordError() {
        return passwordError;
    }

    @Nullable
    Integer getPasswordDiffError() {
        return passwordDiffError;
    }

    boolean isDataValid() {
        return isDataValid;
    }
}