import { cookies } from 'next/headers';
import {
    COOKIE_NAME_ACCESS_TOKEN,
    COOKIE_NAME_REFRESH_TOKEN,
} from '@/constant/constants';
import { isTokenExpired } from '@/lib/jwt';

/**
 * 서버 컴포넌트에서 결정되는 인증 상태.
 *
 * <ul>
 *   <li>{@code AUTHENTICATED}   — access token이 유효 (정상 진입 가능)</li>
 *   <li>{@code REFRESH_ONLY}    — access token 만료/부재, refresh token 유효
 *                                 (진입 허용 → 클라이언트 axios interceptor가 자동 갱신)</li>
 *   <li>{@code UNAUTHENTICATED} — 두 토큰 모두 부재 또는 만료 (로그인 화면으로 보내야 함)</li>
 * </ul>
 */
export type ServerAuthState = 'AUTHENTICATED' | 'REFRESH_ONLY' | 'UNAUTHENTICATED';

/**
 * 쿠키에서 토큰 원문을 그대로 읽어옵니다.
 */
export async function getServerTokens(): Promise<{
    accessToken: string | null;
    refreshToken: string | null;
}> {
    const store = await cookies();
    return {
        accessToken: store.get(COOKIE_NAME_ACCESS_TOKEN)?.value ?? null,
        refreshToken: store.get(COOKIE_NAME_REFRESH_TOKEN)?.value ?? null,
    };
}

/**
 * 토큰 존재 + JWT `exp`까지 함께 확인해 인증 상태를 결정합니다.
 *
 * <p>서버 컴포넌트 진입 시점에 호출 → 결과에 따라 {@code redirect('/login')} 처리.
 */
export async function getServerAuthState(): Promise<ServerAuthState> {
    const { accessToken, refreshToken } = await getServerTokens();

    const accessValid  = !isTokenExpired(accessToken);
    const refreshValid = !isTokenExpired(refreshToken);

    if (accessValid)  return 'AUTHENTICATED';
    if (refreshValid) return 'REFRESH_ONLY';
    return 'UNAUTHENTICATED';
}
