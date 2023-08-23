package dev.pjc1991.commerce.member.point.exception;

import java.io.Serial;

/**
 * 회원 적립금 상세내역의 합산이 비정상적일 때 발생하는 예외입니다.
 */
public class MemberPointAmountBrokenException extends IllegalStateException implements MemberPointExceptionInterface{

    @Serial
    private static final long serialVersionUID = 1L;

    public MemberPointAmountBrokenException(String message) {
    }
}
