package com.revy.authapp.web.controller.api.user;

import com.revy.authapp.security.UserPrincipal;
import com.revy.authapp.web.controller.payload.DefaultPayload;
import com.revy.authapp.web.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User API", description = "User 관련 API")
public class UserApi {

    private final AuthService authService;

    /**
     * 로그아웃을 처리한다.
     *
     * @param authorization 로그아웃 요청
     * @return 메시지 응답
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃")
    public ResponseEntity<DefaultPayload.Res> logout(
            @Valid @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        authService.logout(authorization);
        return ResponseEntity.ok(new DefaultPayload.Res("로그아웃이 완료되었습니다."));
    }

    /**
     * 회원 탈퇴를 처리한다.
     *
     * @param principal 인증 사용자
     * @return 메시지 응답
     */
    @DeleteMapping("/withdraw")
    @Operation(summary = "회원 탈퇴")
    public ResponseEntity<DefaultPayload.Res> withdraw(@AuthenticationPrincipal UserPrincipal principal) {
        authService.withdraw(principal.getId());
        return ResponseEntity.ok(new DefaultPayload.Res("회원 탈퇴가 완료되었습니다."));
    }

//    /**
//     * 로그인한 사용자 요약 정보를 조회한다.
//     *
//     * @param principal 인증 사용자
//     * @return 사용자 요약 정보
//     */
//    @GetMapping("/me")
//    @Operation(summary = "내 정보 조회")
//    public ResponseEntity<UserSummaryResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
//        return ResponseEntity.ok(authService.getSummary(principal.getUsername()));
//    }
}
