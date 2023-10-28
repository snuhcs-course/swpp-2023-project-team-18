package snu.swpp.moment.exception;

import androidx.annotation.Nullable;

public class InvalidScoreSaveRequestException extends RuntimeException{

    @Nullable
    @Override
    public String getMessage() {
        return "잘못된 점수 수정 요청입니다.";
    }
}
