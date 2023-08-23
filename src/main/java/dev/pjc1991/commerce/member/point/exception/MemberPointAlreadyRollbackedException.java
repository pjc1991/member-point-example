package dev.pjc1991.commerce.member.point.exception;

import java.io.Serial;

/**
 * 회원 적립금 적립/사용 내역이 이미 취소되었을 때 발생하는 예외입니다.
 */
public class MemberPointAlreadyRollbackedException extends IllegalStateException implements MemberPointExceptionInterface {

    @Serial
    private static final long serialVersionUID = 1L;
    public MemberPointAlreadyRollbackedException(String message) {
        super(message);
    }
}
