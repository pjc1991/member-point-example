package dev.pjc1991.commerce.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {

    private String name;
    private String message;

    public ErrorResponse() {
        this.name = "Exception";
        this.message = "알 수 없는 에러가 발생했습니다.";
    }

    public ErrorResponse(Throwable exception) {
        this.name = exception.getClass().getSimpleName();
        this.message = exception.getMessage();
    }
}
