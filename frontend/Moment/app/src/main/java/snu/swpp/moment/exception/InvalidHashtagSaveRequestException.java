package snu.swpp.moment.exception;

import androidx.annotation.Nullable;

public class InvalidHashtagSaveRequestException extends RuntimeException{

    @Nullable
    @Override
    public String getMessage() {
        return "잘못된 스토리 ID입니다.";
    }
}
