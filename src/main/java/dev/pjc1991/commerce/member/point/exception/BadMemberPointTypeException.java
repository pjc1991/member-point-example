package dev.pjc1991.commerce.member.point.exception;

import java.io.Serial;

/**
 * 회원 적립금 타입이 잘못된 경우 발생하는 예외
 */
public class BadMemberPointTypeException extends IllegalArgumentException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BadMemberPointTypeException(String message) {
        super(message);
    }

}
