package snu.swpp.moment.exception;

import androidx.annotation.Nullable;

public class InvalidStoryCompletionTimeException extends RuntimeException{

    @Nullable
    @Override
    public String getMessage() {
        return "하루 마무리 가능 기한이 지났습니다.";
    }
}
