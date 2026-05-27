'use client';

import { create } from 'zustand';
import { metaService } from '@/features/meta/service';
import type { CodeInfo, CodeMap } from '@/types';

/**
 * 백엔드 `ExposedEnum` 코드 메타를 보관하는 인메모리 스토어.
 *
 * <p>특성
 * <ul>
 *   <li>localStorage 미사용 — 새로고침/탭 종료 시 자연 GC</li>
 *   <li>{@link CodeProvider}가 마운트되면 즉시 1회 fetch 후 5분 간격 폴링</li>
 *   <li>탭 복귀(window focus) 시 즉시 1회 갱신</li>
 *   <li>{@link useCodeOptions}, {@link useCodeLabel} hook으로 selective subscription</li>
 * </ul>
 */
interface CodeState {
    codes: CodeMap;
    loaded: boolean;
    loading: boolean;
    error: string | null;

    /** 코드 메타 재조회 (중복 호출은 in-flight 가드) */
    refresh: () => Promise<void>;

    /** key(enum 클래스명)에 해당하는 옵션 목록 — 없으면 빈 배열 */
    getOptions: (key: string) => CodeInfo[];

    /** key + code → label 변환 — 없으면 code 그대로 반환 */
    getLabel: (key: string, code: string | null | undefined) => string;
}

export const useCodeStore = create<CodeState>((set, get) => ({
    codes: {},
    loaded: false,
    loading: false,
    error: null,

    async refresh() {
        if (get().loading) return;
        set({ loading: true, error: null });
        try {
            const codes = await metaService.getCodes();
            set({ codes, loaded: true, loading: false });
        } catch (e) {
            set({
                loading: false,
                error: e instanceof Error ? e.message : 'failed to load codes',
            });
        }
    },

    getOptions(key) {
        return get().codes[key] ?? [];
    },

    getLabel(key, code) {
        if (!code) return '';
        const found = get().codes[key]?.find((c) => c.code === code);
        return found?.label ?? code;
    },
}));
