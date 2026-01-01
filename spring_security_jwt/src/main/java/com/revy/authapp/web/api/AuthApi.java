package com.revy.authapp.web.api;

import com.revy.authapp.web.api.payload.LoginPayload;
import com.revy.authapp.web.api.payload.SignupPayload;
import com.revy.authapp.web.service.AuthService;
import com.revy.authapp.web.service.dto.LoginResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 API를 제공하는 컨트롤러.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증 API", description = "회원가입/로그인/로그아웃/토큰 재발급/회원탈퇴")
public class AuthApi {

    private final AuthService authService;

    /**
     * 회원가입을 처리한다.
     *
     * @param req 회원가입 요청
     * @return 회원가입 결과
     */
    @Operation(summary = "회원가입", description = "이메일 기반 회원가입을 진행한다.")
    @PostMapping("/signup")
    public ResponseEntity<SignupPayload.Res> signup(@Valid @RequestBody SignupPayload.Req req) {
        Long userId = authService.signup(req);
        return ResponseEntity.ok(new SignupPayload.Res(userId, "회원가입이 완료되었습니다."));
    }


    /**
     * 로그인과 토큰 발급을 처리한다.
     *
     * @param req 로그인 요청
     * @return 토큰 응답
     */
    @Operation(summary = "로그인", description = "로그인 후 액세스/리프레시 토큰을 발급한다.")
    @PostMapping("/login")
    public ResponseEntity<LoginPayload.Res> login(@Valid @RequestBody LoginPayload.Req req) {
        LoginResult result = authService.login(req);
        return ResponseEntity.ok(LoginPayload.Res.from(result));
    }

}