package com.revy.common.error;

public class ApiException extends RuntimeException {

    private ErrorCode errorCode;
    private final String code;

    public ApiException(String code, String message) {
        super(message);
        this.code = code;
    }

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.code = errorCode.getCode();
    }
    public ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.code = errorCode.getCode();
    }

    public String getCode() {
        return code;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
