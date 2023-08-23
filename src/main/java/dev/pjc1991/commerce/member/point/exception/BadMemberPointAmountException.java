package dev.pjc1991.commerce.member.point.exception;

/**
 * 회원 적립금 생성/사용 요청시 잘못된 적립금 금액이 들어온 경우 발생하는 예외
 */
public class BadMemberPointAmountException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public BadMemberPointAmountException(String message) {
        super(message);
    }

    public BadMemberPointAmountException(String message, Throwable cause) {
        super(message, cause);
    }
}
