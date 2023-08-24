package dev.pjc1991.commerce.member.point.exception;


import java.io.Serial;

/**
 * 동시성 문제가 발생했을 때 발생하는 예외입니다.
 */
public class MemberPointConcurrentException extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 1L;

    public MemberPointConcurrentException(String message) {
        super(message);
    }
}
