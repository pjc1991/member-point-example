package dev.pjc1991.commerce.member.point.exception;

import java.io.Serial;

/**
 * 회원 적립금 FIFO 정책에 따라 적립금이 사용되지 않았음이 확인 되었을 경우 발생하는 예외
 */
public class MemberPointNoFirstInFirstOutException extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 1L;

    public MemberPointNoFirstInFirstOutException(String message) {
        super(message);
    }

    public MemberPointNoFirstInFirstOutException(String message, Throwable cause) {
        super(message, cause);
    }
}
