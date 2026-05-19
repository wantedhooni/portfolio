import {BASE_API} from '@/constant/constants';
import axios, {AxiosError, InternalAxiosRequestConfig} from 'axios';
import {COOKIE_KEYS, getToken, setToken, clearTokens} from '@/lib/authStore-local';
import type {ApiResponse, UserAuthResponse, RefreshTokenRequest} from '@/types';

export const BASE_URL = BASE_API;

interface RetryConfig extends InternalAxiosRequestConfig {
    _retry?: boolean;
}

export const api = axios.create({
    baseURL: BASE_URL,
    headers: {'Content-Type': 'application/json'},
    withCredentials: true,
});

api.interceptors.request.use((config) => {
    const token = getToken(COOKIE_KEYS.accessToken);
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// plain axios — avoids response interceptor loop
export async function callRefresh(refreshToken: string): Promise<UserAuthResponse> {
    const {data} = await axios.post<ApiResponse<UserAuthResponse>>(
        `${BASE_URL}/api/v1/auth/refresh`,
        {refreshToken} satisfies RefreshTokenRequest,
        {headers: {'Content-Type': 'application/json'}},
    );
    return data.data;
}

let isRefreshing = false;
let pendingQueue: Array<{
    resolve: (value: string) => void;
    reject: (reason: unknown) => void;
}> = [];

function processQueue(error: unknown, token: string | null) {
    pendingQueue.forEach(({resolve, reject}) => {
        if (error) reject(error);
        else resolve(token!);
    });
    pendingQueue = [];
}

api.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
        const originalRequest = error.config as RetryConfig;

        if (error.response?.status !== 401 || originalRequest._retry) {
            return Promise.reject(error);
        }

        if (isRefreshing) {
            return new Promise((resolve, reject) => {
                pendingQueue.push({resolve, reject});
            }).then((token) => {
                originalRequest.headers!.Authorization = `Bearer ${token}`;
                return api(originalRequest);
            });
        }

        originalRequest._retry = true;
        isRefreshing = true;

        try {
            const refreshToken = getToken(COOKIE_KEYS.refreshToken);
            if (!refreshToken) throw new Error('No refresh token');

            const {accessToken: newAccessToken, refreshToken: newRefreshToken} =
                await callRefresh(refreshToken);

            setToken(COOKIE_KEYS.accessToken, newAccessToken);
            setToken(COOKIE_KEYS.refreshToken, newRefreshToken);
            processQueue(null, newAccessToken);

            originalRequest.headers!.Authorization = `Bearer ${newAccessToken}`;
            return api(originalRequest);
        } catch (refreshError) {
            processQueue(refreshError, null);
            clearTokens();
            if (typeof window !== 'undefined') {
                window.location.href = '/login';
            }
            return Promise.reject(refreshError);
        } finally {
            isRefreshing = false;
        }
    },
);
