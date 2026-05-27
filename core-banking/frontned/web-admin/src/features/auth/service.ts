import axios from 'axios';
import { api, BASE_URL, callRefresh } from '@/lib/api';
import { COOKIE_KEYS, getToken, setToken, clearTokens } from '@/lib/authStore-local';
import { isTokenExpired } from '@/lib/jwt';
import type { ApiResponse, AdminAuthResponse } from '@/types';

export type AuthStatus = 'authenticated' | 'unauthenticated';

/**
 * 쿠키 + JWT `exp` 기준으로 인증 상태를 확인합니다.
 *
 * <ol>
 *   <li>access token이 만료 전이면 → authenticated</li>
 *   <li>access 만료/부재 & refresh 유효 → 즉시 refresh 시도</li>
 *   <li>둘 다 만료/부재 또는 refresh 실패 → unauthenticated (쿠키 정리)</li>
 * </ol>
 */
export async function resolveAuth(): Promise<AuthStatus> {
  const accessToken = getToken(COOKIE_KEYS.accessToken);
  if (!isTokenExpired(accessToken)) return 'authenticated';

  const refreshToken = getToken(COOKIE_KEYS.refreshToken);
  if (isTokenExpired(refreshToken)) {
    clearTokens();
    return 'unauthenticated';
  }

  try {
    const { accessToken: newAccess, refreshToken: newRefresh } = await callRefresh(refreshToken!);
    setToken(COOKIE_KEYS.accessToken, newAccess);
    setToken(COOKIE_KEYS.refreshToken, newRefresh);
    return 'authenticated';
  } catch {
    clearTokens();
    return 'unauthenticated';
  }
}

export async function login(email: string, password: string): Promise<void> {
  const { data } = await axios.post<ApiResponse<AdminAuthResponse>>(
    `${BASE_URL}/api/v1/auth/login`,
    { email, password },
    { withCredentials: true },
  );
  setToken(COOKIE_KEYS.accessToken, data.data.accessToken);
  setToken(COOKIE_KEYS.refreshToken, data.data.refreshToken);
}

export async function logout(): Promise<void> {
  const refreshToken = getToken(COOKIE_KEYS.refreshToken);
  try {
    await api.post('/api/v1/auth/logout', { refreshToken });
  } finally {
    clearTokens();
  }
}
