'use client';

import type { IGetRowsParams } from 'ag-grid-community';
import { api } from '@/lib/api';
import type { ApiResponse } from '@/types';
import type {
  BatchJobExecution,
  BatchJobExecutionDetail,
  BatchSummary,
} from './types';

const BASE = '/api/v1/batch';

export const batchService = {
  async getSummary(): Promise<BatchSummary> {
    const { data } = await api.get<ApiResponse<BatchSummary>>(`${BASE}/summary`);
    return data.data as BatchSummary;
  },

  async getJobNames(): Promise<string[]> {
    const { data } = await api.get<ApiResponse<string[]>>(`${BASE}/job-names`);
    return data.data ?? [];
  },

  async getExecutions(params?: {
    jobName?: string;
    status?: string;
    limit?: number;
  }): Promise<BatchJobExecution[]> {
    const { data } = await api.get<ApiResponse<BatchJobExecution[]>>(`${BASE}/executions`, {
      params,
    });
    return data.data ?? [];
  },

  /**
   * ag-Grid(infinite row model) datasource 어댑터.
   * 백엔드 `/executions` 는 limit 기반 단일 리스트를 반환하므로
   * 클라이언트에서 startRow/endRow 로 슬라이스한다.
   */
  rowsFetcher(filters: { jobName?: string; status?: string }) {
    return (params: IGetRowsParams) => {
      batchService
        .getExecutions({
          jobName: filters.jobName || undefined,
          status: filters.status || undefined,
          limit: 500,
        })
        .then((all) => {
          const slice = all.slice(params.startRow, params.endRow);
          const lastRow = params.endRow >= all.length ? all.length : -1;
          params.successCallback(slice, lastRow);
        })
        .catch(() => params.failCallback());
    };
  },

  async getExecutionDetail(id: number | string): Promise<BatchJobExecutionDetail> {
    const { data } = await api.get<ApiResponse<BatchJobExecutionDetail>>(
      `${BASE}/executions/${id}`,
    );
    return data.data as BatchJobExecutionDetail;
  },
};
