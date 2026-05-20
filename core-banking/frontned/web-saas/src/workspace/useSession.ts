"use client";

import { useCallback, useState } from "react";

import { COOKIE_KEYS, clearTokens, getToken, setToken } from "@/shared/auth/tokenStore";
import { authService } from "@/features/auth/auth.service";
import type { LoginRequest } from "@/features/auth/auth.types";

/**
 * 로그인 / 로그아웃 세션 상태를 관리하는 훅입니다.
 *
 * 토큰은 쿠키에 저장되며, login 성공 시 accessToken / refreshToken을 기록합니다.
 * logout 시에는 서버에 refresh token 폐기 요청 후 로컬 쿠키를 삭제합니다.
 */
export function useSession() {
  const [isLoading, setIsLoading] = useState(false);

  function hasToken(): boolean {
    return Boolean(getToken(COOKIE_KEYS.accessToken));
  }

  const login = useCallback(async (payload: LoginRequest): Promise<void> => {
    setIsLoading(true);
    try {
      const auth = await authService.login(payload);
      setToken(COOKIE_KEYS.accessToken, auth.accessToken);
      setToken(COOKIE_KEYS.refreshToken, auth.refreshToken);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const logout = useCallback(async (): Promise<void> => {
    const refreshToken = getToken(COOKIE_KEYS.refreshToken);
    try {
      if (refreshToken) await authService.logout({ refreshToken });
    } catch {
      // 토큰이 이미 만료된 경우 무시
    } finally {
      clearTokens();
    }
  }, []);

  return { isLoading, hasToken, login, logout };
}
