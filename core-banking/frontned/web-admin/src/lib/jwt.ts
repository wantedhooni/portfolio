/**
 * JWT 클라이언트 사이드 유틸 — **서명 검증은 수행하지 않습니다.**
 *
 * <p>토큰 무결성은 백엔드(서명 키 보유)의 책임이고, 프론트엔드는
 * 만료 여부만 빠르게 판단해 UX(로그인 화면 리다이렉트 등)에 활용합니다.
 *
 * <p>서버/클라이언트 양쪽에서 동작하도록 `Buffer` / `atob` 둘 다 지원합니다.
 */

export interface JwtPayload {
    /** Unix epoch seconds */
    exp?: number;
    iat?: number;
    sub?: string;
    [key: string]: unknown;
}

/**
 * JWT 페이로드를 파싱합니다. 형식이 잘못되면 `null`.
 */
export function decodeJwt(token: string): JwtPayload | null {
    try {
        const parts = token.split('.');
        if (parts.length !== 3) return null;

        // base64url → base64 변환 + 패딩 보정
        const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
        const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4);

        const json =
            typeof atob !== 'undefined'
                ? atob(padded)
                : Buffer.from(padded, 'base64').toString('utf-8');

        return JSON.parse(json) as JwtPayload;
    } catch {
        return null;
    }
}

/**
 * 토큰 만료 여부.
 *
 * @param token  JWT 문자열 (null/빈값이면 만료 처리)
 * @param leewaySec  시계 오차/네트워크 지연 대비 leeway (기본 5초)
 *                   — exp 직전 토큰을 미리 만료로 간주해 race condition 방지
 */
export function isTokenExpired(token: string | null | undefined, leewaySec = 5): boolean {
    if (!token) return true;
    const payload = decodeJwt(token);
    if (!payload?.exp) return true; // exp 없는 토큰은 신뢰 불가 → 만료 취급
    const nowSec = Math.floor(Date.now() / 1000);
    return nowSec >= payload.exp - leewaySec;
}
