package dev.pjc1991.commerce.member.point.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 회원 적립금 사용시 무한루프가 발생하는 경우 발생하는 예외
 * 이 예외가 발생했다면 회원 적립금 사용 매커니즘에 문제가 있는 것입니다.
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "회원 적립금 사용시 무한루프가 발생했습니다.")
public class MemberPointUseInfiniteLoopException extends RuntimeException {
    public MemberPointUseInfiniteLoopException(String message) {
        super(message);
    }
}
