package snu.swpp.moment.exception;

import androidx.annotation.Nullable;

public class UnknownErrorException extends Exception {

    @Nullable
    @Override
    public String getMessage() {
        return "알 수 없는 오류";
    }
}
