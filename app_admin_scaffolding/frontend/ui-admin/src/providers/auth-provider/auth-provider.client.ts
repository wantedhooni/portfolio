"use client";

import type { AuthProvider } from "@refinedev/core";

import {
  clearAuthTokens,
  getAccessToken,
  getJwtPayload,
  getRefreshToken,
  isJwtExpired,
  setAuthTokens,
  type AuthTokens,
} from "@providers/auth-provider/token-store";
import { apiClient, reissueWithRefreshToken } from "@providers/http-client";

type ApiResponse<T> = {
  success: boolean;
  data: T;
  error?: {
    message?: string;
  };
};

export const authProviderClient: AuthProvider = {
  login: async ({ email, username, password }) => {
    try {
      const loginEmail = email ?? username;

      if (!loginEmail || !password) {
        return {
          success: false,
          error: {
            name: "LoginError",
            message: "email/password is required",
          },
        };
      }

      const response = await apiClient.post<ApiResponse<AuthTokens>>(
        "/api/auth/login",
        {
          email: loginEmail,
          password,
        },
      );

      if (!response.data.success || !response.data.data) {
        return {
          success: false,
          error: {
            name: "LoginError",
            message: response.data.error?.message ?? "Login failed",
          },
        };
      }

      setAuthTokens(response.data.data);

      if (typeof window !== "undefined") {
        // Force a full navigation so server components read freshly stored cookies.
        window.location.replace("/");
      }

      return {
        success: true,
        redirectTo: "/",
      };
    } catch (error: any) {
      clearAuthTokens();

      return {
        success: false,
        error: {
          name: "LoginError",
          message: error?.message ?? "Invalid username or password",
        },
      };
    }
  },
  logout: async () => {
    clearAuthTokens();

    return {
      success: true,
      redirectTo: "/login",
    };
  },
  check: async () => {
    const accessToken = getAccessToken();
    const refreshToken = getRefreshToken();

    if (accessToken && !isJwtExpired(accessToken)) {
      return {
        authenticated: true,
      };
    }

    if (refreshToken) {
      try {
        const refreshed = await reissueWithRefreshToken();

        if (refreshed?.accessToken) {
          return {
            authenticated: true,
          };
        }
      } catch {
        clearAuthTokens();
      }
    }

    return {
      authenticated: false,
      logout: true,
      redirectTo: "/login",
    };
  },
  getPermissions: async () => {
    const accessToken = getAccessToken();

    if (!accessToken) {
      return null;
    }

    const payload = getJwtPayload(accessToken);
    const roleNames = payload?.roleNames;

    if (Array.isArray(roleNames)) {
      return roleNames;
    }

    return ["ROLE_ADMIN"];
  },
  getIdentity: async () => {
    const accessToken = getAccessToken();

    if (!accessToken) {
      return null;
    }

    const payload = getJwtPayload(accessToken);
    const userId = payload?.userId;

    return {
      id: typeof userId === "string" ? userId : "unknown",
      name: typeof userId === "string" ? userId : "Admin",
    };
  },
  onError: async (error) => {
    if (error?.statusCode === 401 || error?.response?.status === 401) {
      clearAuthTokens();
      return {
        logout: true,
      };
    }

    return { error };
  },
};
