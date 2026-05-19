/** 공통 API 응답 래퍼 */
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
  timestamp: number;
}

/** 페이지네이션 요청 파라미터 */
export interface Pageable {
  page: number;
  size: number;
  sort?: string[];
}

/** 페이지네이션 응답 래퍼 */
export interface ApiPageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

/** JWT 액세스·리프레시 토큰 응답 */
export interface UserAuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

/** 리프레시 토큰 갱신 요청 */
export interface RefreshTokenRequest {
  refreshToken: string;
}
