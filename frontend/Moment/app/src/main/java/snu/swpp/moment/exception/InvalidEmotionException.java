package snu.swpp.moment.exception;

import androidx.annotation.Nullable;

public class InvalidEmotionException extends Exception {

    @Nullable
    @Override
    public String getMessage() {
        return "처리할 수 없는 감정입니다.";
    }
}
