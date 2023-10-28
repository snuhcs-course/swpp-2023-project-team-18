package snu.swpp.moment.exception;

import androidx.annotation.Nullable;

public class AIStoryRetrievalFailureException extends RuntimeException {

    @Nullable
    @Override
    public String getMessage() {
        return "서버로부터 응답을 받는 데 실패하였습니다.";
    }
}
