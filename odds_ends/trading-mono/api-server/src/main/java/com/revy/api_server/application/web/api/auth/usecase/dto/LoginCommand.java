package com.revy.api_server.application.web.api.auth.usecase.dto;

public interface LoginCommand {
    public String getEmail();
    public String getPassword();
}
