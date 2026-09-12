package com.revy.api_server.application.exception;

import com.revy.common.error.ApiException;

public class TradeException extends ApiException {
    public TradeException(String code, String message) {
        super(code, message);
    }

    public TradeException(TracdeErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }
    public TradeException(String message) {
        super(TracdeErrorCode.TRADE_UNDEFINED_ERROR.getCode(), message);
    }

    public TradeException(TracdeErrorCode errorCode, String message) {
        super(errorCode.getCode(), message);
    }

    public enum TracdeErrorCode {
        TRADE_NOT_FOUND("TRADE-001", "Trade not found"),
        INSUFFICIENT_FUNDS("TRADE-002", "Insufficient funds for the trade"),
        INVALID_TRADE_TYPE("TRADE-003", "Invalid trade type"),
        TRADE_LIMIT_EXCEEDED("TRADE-004", "Trade limit exceeded"),
        TRADE_CURRENCY_MISMATCH("TRADE-005", "Trade currency mismatch"),
        ORDER_NOT_FOUND("TRADE-006", "Order not found"),
        INVALID_ORDER_STATUS("TRADE-007", "Invalid order status for the operation"),
        POSITION_NOT_FOUND("TRADE-008", "Position not found"),
        DUPLICATE_TRADE("TRADE-009", "Duplicate trade detected"),
        //미정의에러
        TRADE_UNDEFINED_ERROR("TRADE-999", "Undefined trade error"),


        ;
        private final String code;
        private final String message;

        TracdeErrorCode(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}

