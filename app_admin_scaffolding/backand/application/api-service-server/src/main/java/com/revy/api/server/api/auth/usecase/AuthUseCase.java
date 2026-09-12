package com.revy.api.server.api.auth.usecase;


import com.revy.api.server.api.auth.payload.LoginPayload;
import com.revy.api.server.api.auth.payload.SignupPayload;
import com.revy.api.server.api.auth.payload.TokenReissuePayload;

public interface AuthUseCase {
    SignupPayload.Res signup(SignupPayload.Req req);

    LoginPayload.Res login(LoginPayload.Req req);

    TokenReissuePayload.Res reissue(TokenReissuePayload.Req req);
}
