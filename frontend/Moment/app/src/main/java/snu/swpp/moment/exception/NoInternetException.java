package snu.swpp.moment.exception;

import androidx.annotation.Nullable;

public class NoInternetException extends Exception {

    @Nullable
    @Override
    public String getMessage() {
        return "서버와의 연결이 불안정합니다.";
    }
}
