package dev.pjc1991.commerce.member.point.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 회원 적립금이 부족하는 경우 발생하는 예외
 */
@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "Not enough point")
public class NotEnoughPointException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NotEnoughPointException(String message) {
        super(message);
    }

    public NotEnoughPointException(String message, Throwable cause) {
        super(message, cause);
    }
}
