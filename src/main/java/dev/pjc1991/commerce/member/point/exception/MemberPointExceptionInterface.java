package dev.pjc1991.commerce.member.point.exception;

import java.io.Serializable;

/**
 * 회원 적립금 예외 처리 인터페이스
 * 예외 처리시 예외 이름과 메시지를 반환한다.
 */
public interface MemberPointExceptionInterface extends Serializable {

    default String getName(){
        return this.getClass().getSimpleName();
    }
    String getMessage();

}
