package dev.pjc1991.commerce.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {

    private String name;
    private String message;

    public ErrorResponse(Throwable throwable) {
        this.name = throwable.getClass().getSimpleName();
        this.message = throwable.getMessage();
    }
}
