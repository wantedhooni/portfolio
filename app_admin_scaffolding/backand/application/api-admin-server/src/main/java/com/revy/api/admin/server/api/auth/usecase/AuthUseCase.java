package com.revy.api.admin.server.api.auth.usecase;

import com.revy.api.admin.server.api.auth.payload.SignupPayload;
import com.revy.api.admin.server.api.auth.payload.LoginPayload;
import com.revy.api.admin.server.api.auth.payload.TokenReissuePayload;

public interface AuthUseCase {

    LoginPayload.Res login(LoginPayload.Req req);

    TokenReissuePayload.Res reissue(TokenReissuePayload.Req req);
}
