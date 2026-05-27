'use client';

import { useCodeStore } from '@/lib/codeStore';
import type { CodeInfo } from '@/types';

/** Stable empty array — same reference on every call to avoid useSyncExternalStore infinite loop */
const EMPTY_OPTIONS: CodeInfo[] = [];

/**
 * enum 클래스명에 해당하는 옵션 배열을 반환합니다.
 *
 * @example
 * const options = useCodeOptions('JobType');
 * // [{ code: 'SETTLEMENT', label: '정산' }, ...]
 */
export function useCodeOptions(key: string): CodeInfo[] {
    return useCodeStore((s) => s.codes[key] ?? EMPTY_OPTIONS);
}

/**
 * key + code 조합을 화면 표시 라벨로 변환합니다.
 * 캐시에 없으면 code를 그대로 반환합니다.
 *
 * @example
 * const label = useCodeLabel('JobType', row.jobType);  // 'SETTLEMENT' → '정산'
 */
export function useCodeLabel(key: string, code: string | null | undefined): string {
    return useCodeStore((s) => {
        if (!code) return '';
        return s.codes[key]?.find((c) => c.code === code)?.label ?? code;
    });
}

/** 코드 메타 로딩 상태 — 초기 스피너 표시용 */
export function useCodeLoaded(): boolean {
    return useCodeStore((s) => s.loaded);
}
