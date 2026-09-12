package com.revy.api_server.application.web.api.account.payload;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public class TransferPayload {

    @Schema(name = "TransferPayload.Req")
    public record Req(
            String fromAccountNo,
            String toAccountNo,
            BigDecimal amount,
            // 양수
            String referenceId,
            String fromDescription,
            String toDescription
    ) {
    }


    @Schema(name = "TransferPayload.Res")
    public record Res(
            String fromAccountNo,
            String toAccountNo,
            BigDecimal amount,
            BigDecimal afterBalance,
            // 양수
            String referenceId
    ) {


    }

}
