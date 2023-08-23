package dev.pjc1991.commerce.member.point.exception;


import java.io.Serial;

/**
 * 회원 적립금 만료일이 비정상적으로 설정 되어있을 경우 발생하는 예외
 */
public class BadMemberPointExpireDateException extends IllegalArgumentException implements MemberPointExceptionInterface {

    @Serial
    private static final long serialVersionUID = 1L;

    public BadMemberPointExpireDateException(String message) {
        super(message);
    }

    public BadMemberPointExpireDateException(String message, Throwable cause) {
        super(message, cause);
    }

}
