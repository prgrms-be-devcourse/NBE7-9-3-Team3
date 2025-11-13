package org.example.backend.global.exception;

import lombok.Getter;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.HttpStatus;

@Getter
public class ServiceException extends RuntimeException {

    private final String resultCode;
    private final String msg;
    private final HttpStatus httpStatus;

    public ServiceException(String resultCode, String msg, HttpStatus httpStatus) {
        super("%s : %s".formatted(resultCode, msg));
        this.resultCode = resultCode;
        this.msg = msg;
        this.httpStatus = httpStatus;
    }
    public ApiResponse<Void> getApiResponse() {
        return new ApiResponse<>(resultCode, msg);
    }
}
