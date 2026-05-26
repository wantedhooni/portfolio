package com.revy.client.exception;

public class FrankfurterApiException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public FrankfurterApiException(int statusCode, String responseBody) {
        super("Frankfurter API request failed. statusCode=" + statusCode + ", body=" + responseBody);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int statusCode() {
        return statusCode;
    }

    public String responseBody() {
        return responseBody;
    }
}
