export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
  timestamp: number;
}

export interface AdminAuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface LogoutRequest {
  refreshToken?: string;
}

export interface AdminRequest {
  email: string;
  name: string;
  password: string;
}

export interface AdminItem {
  id: number;
  email: string;
  name: string;
  [key: string]: unknown;
}

export interface AccountItem {
  id: number;
  [key: string]: unknown;
}

export interface AccountTransactionItem {
  id: number;
  [key: string]: unknown;
}

export interface Pageable {
  page: number;
  size: number;
  sort?: string[];
}

export interface ApiPageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface JwtPrincipal {
  [key: string]: unknown;
}

export type { DetailPageConfig, DetailField } from './page-config';
