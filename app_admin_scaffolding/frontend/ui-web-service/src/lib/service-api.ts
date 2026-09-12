import type {
  ApiEndpoint,
  ApiResponse,
  ReissueRequest,
  SigninRequest,
  ServiceApiStatus,
  SignupRequest,
  SignupResponse,
  TokenResponse,
} from "@/lib/types";

function getApiBaseUrl() {
  return process.env.SERVICE_API_BASE_URL || "http://localhost:9090";
}

async function serviceFetch<T>(path: string, body: unknown) {
  const response = await fetch(`${getApiBaseUrl()}${path}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
    cache: "no-store",
  });

  const payload = (await response.json()) as ApiResponse<T>;

  if (!response.ok || !payload.success) {
    throw new Error(payload.error?.message || "API 요청에 실패했습니다.");
  }

  return payload;
}

export async function signup(input: SignupRequest) {
  return serviceFetch<SignupResponse>("/api/auth/signup", input);
}

export async function signin(input: SigninRequest) {
  return serviceFetch<TokenResponse>("/api/auth/login", input);
}

export async function reissue(input: ReissueRequest) {
  return serviceFetch<TokenResponse>("/api/auth/reissue", input);
}

export async function getServiceApiStatus(): Promise<ServiceApiStatus> {
  const baseUrl = getApiBaseUrl();
  const endpoints: ApiEndpoint[] = [
    {
      method: "POST",
      path: "/api/auth/signup",
      summary: "회원 가입",
    },
    {
      method: "POST",
      path: "/api/auth/login",
      summary: "로그인",
    },
    {
      method: "POST",
      path: "/api/auth/reissue",
      summary: "토큰 재발급",
    },
  ];

  try {
    const response = await fetch(`${baseUrl}/v3/api-docs.yaml`, {
      cache: "no-store",
      signal: AbortSignal.timeout(3000),
    });

    if (!response.ok) {
      throw new Error("openapi unavailable");
    }

    const source = await response.text();
    const title = readYamlValue(source, "title") || "service-api";
    const version = readYamlValue(source, "version") || "v1";

    return {
      baseUrl,
      isReachable: true,
      title,
      version,
      endpoints,
      checkedAt: new Date().toISOString(),
    };
  } catch {
    return {
      baseUrl,
      isReachable: false,
      title: "service-api",
      version: "v1",
      endpoints,
      checkedAt: new Date().toISOString(),
    };
  }
}

function readYamlValue(source: string, key: string) {
  const match = source.match(new RegExp(`^\\s*${key}:\\s*(.+)$`, "m"));
  return match?.[1]?.trim() ?? "";
}
