package dev.pjc1991.commerce.member.exception;

import java.io.Serial;

/**
 * 회원이 존재하지 않을 경우 발생하는 예외
 */
public class MemberNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public MemberNotFoundException(String message) {
        super(message);
    }
}
