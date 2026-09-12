export type ApiError = {
  code?: string;
  message?: string;
  fieldErrors?: Record<string, string>;
};

export type ApiResponse<T> = {
  success: boolean;
  data: T;
  error?: ApiError;
};

export type SignupRequest = {
  email: string;
  password: string;
  nickName: string;
};

export type SigninRequest = {
  email: string;
  password: string;
};

export type SignupResponse = {
  message?: string;
};

export type TokenResponse = {
  tokenType: string;
  accessToken: string;
  refreshToken: string;
};

export type ReissueRequest = {
  refreshToken: string;
};

export type ApiEndpoint = {
  method: "POST";
  path: string;
  summary: string;
};

export type ServiceApiStatus = {
  baseUrl: string;
  isReachable: boolean;
  title: string;
  version: string;
  endpoints: ApiEndpoint[];
  checkedAt: string;
};
