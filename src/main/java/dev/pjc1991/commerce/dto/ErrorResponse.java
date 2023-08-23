package dev.pjc1991.commerce.dto;

import dev.pjc1991.commerce.member.point.exception.MemberPointExceptionInterface;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {

    private String name;
    private String message;

    public ErrorResponse(MemberPointExceptionInterface exception) {
        this.name = exception.getName();
        this.message = exception.getMessage();
    }
}
