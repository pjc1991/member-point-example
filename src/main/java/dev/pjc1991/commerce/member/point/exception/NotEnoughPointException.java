package dev.pjc1991.commerce.member.point.exception;

import java.io.Serial;

/**
 * 회원 적립금이 부족하는 경우 발생하는 예외입니다.
 */
public class NotEnoughPointException extends RuntimeException implements MemberPointExceptionInterface {

    @Serial
    private static final long serialVersionUID = 1L;

    public NotEnoughPointException(String message) {
        super(message);
    }

    public NotEnoughPointException(String message, Throwable cause) {
        super(message, cause);
    }
}
