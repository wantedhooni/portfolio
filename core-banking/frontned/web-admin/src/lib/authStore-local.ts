export const COOKIE_KEYS = {
  accessToken: 'accessToken',
  refreshToken: 'refreshToken',
} as const;

const COOKIE_BASE_OPTS = 'path=/; SameSite=Strict';

export function getToken(key: string): string | null {
  if (typeof document === 'undefined') return null;
  const match = document.cookie.match(new RegExp(`(?:^|;\\s*)${key}=([^;]*)`));
  return match ? decodeURIComponent(match[1]) : null;
}

export function setToken(key: string, value: string, maxAge?: number) {
  const expiry = maxAge != null ? `; max-age=${maxAge}` : '';
  document.cookie = `${key}=${encodeURIComponent(value)}; ${COOKIE_BASE_OPTS}${expiry}`;
}

export function deleteToken(key: string) {
  setToken(key, '', 0);
}

export function clearTokens() {
  deleteToken(COOKIE_KEYS.accessToken);
  deleteToken(COOKIE_KEYS.refreshToken);
}
