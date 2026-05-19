import { api } from "@/shared/api/client";
import type { ApiResponse, UserAuthResponse } from "@/shared/types/api.types";
import type { JwtPrincipal, LoginRequest, LogoutRequest, SignupReceipt, SignupRequest } from "./auth.types";

function unwrap<T>(response: { data: ApiResponse<T> }): T {
  return response.data.data;
}

/**
 * 인증 도메인 API를 호출하는 서비스 클래스입니다.
 * 로그인, 로그아웃, 토큰 갱신, 사용자 정보 조회를 담당합니다.
 */
export class AuthService {
  /**
   * 이메일과 비밀번호로 사용자 토큰을 발급받습니다.
   */
  async login(payload: LoginRequest): Promise<UserAuthResponse> {
    return unwrap(await api.post<ApiResponse<UserAuthResponse>>("/api/v1/auth/login", payload));
  }

  /**
   * 보관 중인 refresh token을 서버에서 만료 처리합니다.
   */
  async logout(payload: LogoutRequest): Promise<unknown> {
    return unwrap(await api.post<ApiResponse<unknown>>("/api/v1/auth/logout", payload));
  }

  /**
   * Authorization 헤더의 토큰으로 현재 사용자 정보를 조회합니다.
   */
  async me(): Promise<JwtPrincipal> {
    return unwrap(await api.get<ApiResponse<JwtPrincipal>>("/api/v1/auth/me"));
  }

  /**
   * 신규 SaaS 사용자 가입을 요청합니다.
   * 현재 명세에 회원가입 엔드포인트가 없으므로 확장 시 연결합니다.
   */
  async signup(payload: SignupRequest): Promise<SignupReceipt> {
    return unwrap(await api.post<ApiResponse<SignupReceipt>>("/api/v1/auth/signup", payload));
  }
}

export const authService = new AuthService();
