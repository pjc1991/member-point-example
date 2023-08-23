package dev.pjc1991.commerce.member.point.exception;

import java.io.Serial;

/**
 * 회원 적립금 이벤트가 존재하지 않을 경우 발생하는 예외
 */
public class MemberPointEventNotFound extends RuntimeException implements MemberPointExceptionInterface {

    @Serial
    private static final long serialVersionUID = 1L;

    public MemberPointEventNotFound(String message) {
        super(message);
    }

    public MemberPointEventNotFound(String message, Throwable cause) {
        super(message, cause);
    }
}
