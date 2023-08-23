package dev.pjc1991.commerce.member.point.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

/**
 * 회원 적립금 타입이 잘못된 경우 발생하는 예외
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadMemberPointTypeException extends IllegalArgumentException implements MemberPointExceptionInterface {

    @Serial
    private static final long serialVersionUID = 1L;

    public BadMemberPointTypeException(String message) {
        super(message);
    }

    public BadMemberPointTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
