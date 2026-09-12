package com.revy.api.server.api.auth;

import com.revy.api.server.api.auth.payload.LoginPayload;
import com.revy.api.server.api.auth.payload.SignupPayload;
import com.revy.api.server.api.auth.payload.TokenReissuePayload;
import com.revy.api.server.api.auth.usecase.AuthUseCase;
import com.revy.common.web.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증 API", description = "사용자 생성 /로그인 /토큰 재발급")
public class AuthApi {

    private final AuthUseCase authUseCase;

    @Operation(summary = "회원 가입", description = "회원 가입을 한다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupPayload.Res>> signup(@Valid @RequestBody SignupPayload.Req req){
        return ResponseEntity.ok(ApiResponse.ok(authUseCase.signup(req)));
    }

    @Operation(summary = "로그인", description = "로그인 후 액세스/리프레시 토큰을 발급한다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginPayload.Res>> login(@Valid @RequestBody LoginPayload.Req req){
        return ResponseEntity.ok(ApiResponse.ok(authUseCase.login(req)));
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenReissuePayload.Res>> reissue(@Valid @RequestBody TokenReissuePayload.Req req){
        return ResponseEntity.ok(ApiResponse.ok(authUseCase.reissue(req)));
    }
}
