'use client';

import { useEffect } from 'react';
import { useCodeStore } from '@/lib/codeStore';

/** 백엔드 enum 메타 폴링 주기 — 백엔드 변경을 평균 2.5분 내 반영 */
const POLL_INTERVAL_MS = 1000 * 60 * 5;

/**
 * 백엔드 `/api/v1/meta/codes` 인메모리 캐시 초기화 + 주기 갱신 Provider.
 *
 * <p>대시보드 레이아웃에 한 번만 마운트하면 됩니다.
 * 자식 컴포넌트는 {@link useCodeStore}의 hook으로 즉시 옵션 사용 가능.
 *
 * <p>동작
 * <ul>
 *   <li>마운트 시 1회 fetch</li>
 *   <li>5분마다 background polling</li>
 *   <li>탭 복귀(window focus) 시 즉시 1회 갱신</li>
 *   <li>localStorage 미사용 — 새로고침 시 다시 fetch</li>
 * </ul>
 */
export function CodeProvider({ children }: { children: React.ReactNode }) {
    const refresh = useCodeStore((s) => s.refresh);

    useEffect(() => {
        refresh();

        const id = window.setInterval(refresh, POLL_INTERVAL_MS);

        const onFocus = () => refresh();
        window.addEventListener('focus', onFocus);

        return () => {
            window.clearInterval(id);
            window.removeEventListener('focus', onFocus);
        };
    }, [refresh]);

    return <>{children}</>;
}
