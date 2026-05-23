'use client';

import { api } from '@/lib/api';
import { fetchPage, type PageResult } from '@/services/crud';
import type { ApiResponse } from '@/types';
import type {
  ExecutionStatus,
  JobHistory,
  QuartzJob,
  RescheduleRequest,
  RunningJob,
  UpsertRequest,
} from './types';

const BASE = '/api/v1/quartz';

export const quartzService = {
  // ── 조회 ────────────────────────────────────────────────────
  async listJobs(): Promise<QuartzJob[]> {
    const { data } = await api.get<ApiResponse<QuartzJob[]>>(`${BASE}/jobs`);
    return data.data ?? [];
  },

  async listRunning(): Promise<RunningJob[]> {
    const { data } = await api.get<ApiResponse<RunningJob[]>>(`${BASE}/jobs/running`);
    return data.data ?? [];
  },

  async historyByJob(
    jobGroup: string,
    jobName: string,
    page = 0,
    size = 30,
  ): Promise<PageResult<JobHistory>> {
    return fetchPage<JobHistory>(`${BASE}/history`, page, size, { jobGroup, jobName });
  },

  async historyByStatus(
    status: ExecutionStatus,
    page = 0,
    size = 30,
  ): Promise<PageResult<JobHistory>> {
    return fetchPage<JobHistory>(`${BASE}/history`, page, size, { status });
  },

  /** 완료 이력(SUCCESS·FAILED·VETOED) — jobName 부분 일치 선택 */
  async historyCompleted(
    page = 0,
    size = 30,
    jobName?: string,
  ): Promise<PageResult<JobHistory>> {
    return fetchPage<JobHistory>(`${BASE}/history/completed`, page, size,
      jobName ? { jobName } : undefined);
  },

  // ── CRUD ────────────────────────────────────────────────────
  async create(req: UpsertRequest): Promise<void> {
    await api.post(`${BASE}/jobs`, req);
  },

  async update(group: string, name: string, req: UpsertRequest): Promise<void> {
    await api.put(`${BASE}/jobs/${encodeURIComponent(group)}/${encodeURIComponent(name)}`, req);
  },

  async reschedule(group: string, name: string, req: RescheduleRequest): Promise<void> {
    await api.patch(
      `${BASE}/jobs/${encodeURIComponent(group)}/${encodeURIComponent(name)}/schedule`,
      req,
    );
  },

  async remove(group: string, name: string): Promise<void> {
    await api.delete(`${BASE}/jobs/${encodeURIComponent(group)}/${encodeURIComponent(name)}`);
  },

  // ── 제어 ────────────────────────────────────────────────────
  async pause(group: string, name: string): Promise<void> {
    await api.post(`${BASE}/jobs/${encodeURIComponent(group)}/${encodeURIComponent(name)}/pause`);
  },

  async resume(group: string, name: string): Promise<void> {
    await api.post(`${BASE}/jobs/${encodeURIComponent(group)}/${encodeURIComponent(name)}/resume`);
  },

  async runNow(group: string, name: string): Promise<void> {
    await api.post(`${BASE}/jobs/${encodeURIComponent(group)}/${encodeURIComponent(name)}/run`);
  },

  /** FAILED 이력 ID 로 해당 Job 즉시 재실행 */
  async retryJob(historyId: number): Promise<void> {
    await api.post(`${BASE}/history/${historyId}/retry`);
  },
};
