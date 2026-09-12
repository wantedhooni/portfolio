package com.revy.api_server.application.web.api.auth.payload;

import io.swagger.v3.oas.annotations.media.Schema;

public class DefaultPayload {

    @Schema(name = "DefaultPayload.Res")
    public record Res(String message) {
    }
}
