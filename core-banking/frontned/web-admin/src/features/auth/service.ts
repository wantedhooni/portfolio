import axios from 'axios';
import { api, BASE_URL, callRefresh } from '@/lib/api';
import { COOKIE_KEYS, getToken, setToken, clearTokens } from '@/lib/authStore-local';
import type { ApiResponse, AdminAuthResponse } from '@/types';

export type AuthStatus = 'authenticated' | 'unauthenticated';

/**
 * 쿠키 기준으로 인증 상태 확인.
 * accessToken 없으면 refreshToken 으로 갱신 시도.
 */
export async function resolveAuth(): Promise<AuthStatus> {
  if (getToken(COOKIE_KEYS.accessToken)) return 'authenticated';

  const refreshToken = getToken(COOKIE_KEYS.refreshToken);
  if (!refreshToken) return 'unauthenticated';

  try {
    const { accessToken, refreshToken: newRefresh } = await callRefresh(refreshToken);
    setToken(COOKIE_KEYS.accessToken, accessToken);
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
