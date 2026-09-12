"use client";

import type { HttpError } from "@refinedev/core";
import axios, {
  AxiosError,
  type AxiosInstance,
  type InternalAxiosRequestConfig,
} from "axios";

import {
  clearAuthTokens,
  getAccessToken,
  getRefreshToken,
  setAuthTokens,
  type AuthTokens,
} from "@providers/auth-provider/token-store";

type ApiErrorBody = {
  message?: string;
};

type ApiResponse<T> = {
  success: boolean;
  data: T;
  error?: ApiErrorBody;
};

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

const REISSUE_PATH = "/api/auth/reissue";

type RetryConfig = InternalAxiosRequestConfig & {
  _retry?: boolean;
};

const toHttpError = (error: AxiosError): HttpError => {
  const data = error.response?.data as
    | ApiResponse<unknown>
    | Record<string, unknown>
    | undefined;

  const apiMessage =
    typeof data === "object" && data !== null
      ? (("error" in data
          ? (data.error as ApiErrorBody | undefined)?.message
          : undefined) ??
        (("message" in data ? data.message : undefined) as string | undefined))
      : undefined;

  return {
    ...error,
    message: apiMessage ?? error.message,
    statusCode: error.response?.status ?? 500,
  };
};

const bareClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

const shouldSkipAuthHeader = (url?: string): boolean => {
  if (!url) {
    return false;
  }

  return url.includes("/api/auth/login") || url.includes(REISSUE_PATH);
};

let isRefreshing = false;
let refreshSubscribers: Array<(token: string | null) => void> = [];

const subscribeTokenRefresh = (callback: (token: string | null) => void) => {
  refreshSubscribers.push(callback);
};

const notifySubscribers = (token: string | null) => {
  refreshSubscribers.forEach((callback) => callback(token));
  refreshSubscribers = [];
};

export const reissueWithRefreshToken = async (): Promise<AuthTokens | null> => {
  const refreshToken = getRefreshToken();

  if (!refreshToken) {
    return null;
  }

  const response = await bareClient.post<ApiResponse<AuthTokens>>(REISSUE_PATH, {
    refreshToken,
  });

  if (!response.data.success || !response.data.data) {
    return null;
  }

  const tokens = response.data.data;
  setAuthTokens(tokens);

  return tokens;
};

apiClient.interceptors.request.use((config) => {
  if (shouldSkipAuthHeader(config.url)) {
    return config;
  }

  const accessToken = getAccessToken();

  if (accessToken) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${accessToken}`;
  }

  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const statusCode = error.response?.status;
    const originalRequest = error.config as RetryConfig | undefined;

    if (
      statusCode !== 401 ||
      !originalRequest ||
      originalRequest._retry ||
      shouldSkipAuthHeader(originalRequest.url)
    ) {
      return Promise.reject(toHttpError(error));
    }

    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        subscribeTokenRefresh((newAccessToken) => {
          if (!newAccessToken) {
            reject(toHttpError(error));
            return;
          }

          originalRequest.headers = originalRequest.headers ?? {};
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
          resolve(apiClient(originalRequest));
        });
      });
    }

    originalRequest._retry = true;
    isRefreshing = true;

    try {
      const refreshedTokens = await reissueWithRefreshToken();

      if (!refreshedTokens?.accessToken) {
        clearAuthTokens();
        notifySubscribers(null);
        return Promise.reject(toHttpError(error));
      }

      notifySubscribers(refreshedTokens.accessToken);
      originalRequest.headers = originalRequest.headers ?? {};
      originalRequest.headers.Authorization = `Bearer ${refreshedTokens.accessToken}`;

      return apiClient(originalRequest);
    } catch (refreshError) {
      clearAuthTokens();
      notifySubscribers(null);

      if (axios.isAxiosError(refreshError)) {
        return Promise.reject(toHttpError(refreshError));
      }

      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  },
);
