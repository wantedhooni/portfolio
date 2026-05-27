'use client';

import { api } from '@/lib/api';
import type { ApiResponse, CodeMap } from '@/types';

const BASE = '/api/v1/meta';

export const metaService = {
    /**
     * 백엔드의 모든 `ExposedEnum` 구현체 옵션을 일괄 조회합니다.
     * 응답 키는 enum 클래스명(예: `JobType`)이며 값은 `{ code, label }[]`.
     */
    async getCodes(): Promise<CodeMap> {
        const { data } = await api.get<ApiResponse<CodeMap>>(`${BASE}/codes`);
        return data.data ?? {};
    },
};
