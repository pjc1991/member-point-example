package dev.pjc1991.commerce.member.point.exception;

import java.io.Serializable;

public interface MemberPointExceptionInterface extends Serializable {

    default String getName(){
        return this.getClass().getSimpleName();
    }
    String getMessage();

}
