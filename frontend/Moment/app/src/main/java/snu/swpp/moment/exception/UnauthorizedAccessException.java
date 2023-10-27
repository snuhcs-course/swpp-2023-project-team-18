package snu.swpp.moment.exception;

public class UnauthorizedAccessException extends RuntimeException {
    public String getMessage() {
        return "토큰이 만료되었습니다.";
    }
}
