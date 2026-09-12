"use client";

import Cookies, { type CookieAttributes } from "js-cookie";
import {
  ACCESS_TOKEN_COOKIE_KEY,
  REFRESH_TOKEN_COOKIE_KEY,
} from "@providers/auth-provider/token-keys";

export type AuthTokens = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
};

const isBrowser = () => typeof window !== "undefined";

const defaultCookieOptions = (): CookieAttributes => ({
  path: "/",
  sameSite: "lax",
  secure: isBrowser() ? window.location.protocol === "https:" : false,
});

const decodeJwtPayload = (token: string): Record<string, unknown> | null => {
  try {
    const payload = token.split(".")[1];
    if (!payload) {
      return null;
    }

    const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), "=");

    const decoded =
      typeof atob === "function"
        ? atob(padded)
        : Buffer.from(padded, "base64").toString("binary");

    return JSON.parse(decoded) as Record<string, unknown>;
  } catch {
    return null;
  }
};

const getExpiryFromToken = (token: string): Date | undefined => {
  const payload = decodeJwtPayload(token);
  const exp = payload?.exp;

  if (typeof exp !== "number") {
    return undefined;
  }

  return new Date(exp * 1000);
};

export const setAuthTokens = ({ accessToken, refreshToken }: AuthTokens): void => {
  if (!isBrowser()) {
    return;
  }

  const options = defaultCookieOptions();

  Cookies.set(ACCESS_TOKEN_COOKIE_KEY, accessToken, {
    ...options,
    expires: getExpiryFromToken(accessToken),
  });

  Cookies.set(REFRESH_TOKEN_COOKIE_KEY, refreshToken, {
    ...options,
    expires: getExpiryFromToken(refreshToken),
  });
};

export const clearAuthTokens = (): void => {
  if (!isBrowser()) {
    return;
  }

  const options = { path: "/" };
  Cookies.remove(ACCESS_TOKEN_COOKIE_KEY, options);
  Cookies.remove(REFRESH_TOKEN_COOKIE_KEY, options);
  Cookies.remove("auth", options);
};

export const getAccessToken = (): string | null => {
  if (!isBrowser()) {
    return null;
  }

  return Cookies.get(ACCESS_TOKEN_COOKIE_KEY) ?? null;
};

export const getRefreshToken = (): string | null => {
  if (!isBrowser()) {
    return null;
  }

  return Cookies.get(REFRESH_TOKEN_COOKIE_KEY) ?? null;
};

export const isJwtExpired = (token: string): boolean => {
  const payload = decodeJwtPayload(token);
  const exp = payload?.exp;

  if (typeof exp !== "number") {
    return true;
  }

  return exp * 1000 <= Date.now();
};

export const getJwtPayload = (token: string): Record<string, unknown> | null => {
  return decodeJwtPayload(token);
};
