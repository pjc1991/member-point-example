package dev.pjc1991.commerce.member.point.exception;

import java.io.Serial;

/**
 * 회원 적립금 상세 내역이 존재하지 않는 경우 발생하는 예외
 */
public class MemberPointDetailNotFoundException extends IllegalArgumentException implements MemberPointExceptionInterface {

    @Serial
    private static final long serialVersionUID = 1L;

    public MemberPointDetailNotFoundException(String message) {
        super(message);
    }

    public MemberPointDetailNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
