package dev.pjc1991.commerce.member.point.exception;

import java.io.Serial;

/**
 * 회원 적립금 사용시 무한루프가 발생하는 경우 발생하는 예외
 * 이 예외가 발생했다면 회원 적립금 사용 매커니즘에 문제가 있는 것입니다.
 * 테스트 시에 발생할 수 있으며, 실제 운영 환경에서는 발생하지 않아야 합니다.
 */
public class MemberPointUseInfiniteLoopException extends RuntimeException implements MemberPointExceptionInterface {
    @Serial
    private static final long serialVersionUID = 1L;

    public MemberPointUseInfiniteLoopException(String message) {
        super(message);
    }

}
