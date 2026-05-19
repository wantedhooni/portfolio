'use client';

import axios from 'axios';
import { api, BASE_URL } from '@/lib/api';
import type { ApiResponse, ApiPageResponse } from '@/types';

export interface PageResult<T> {
  content: T[];
  totalElements: number;
}

export async function fetchPage<T>(
  endpoint: string,
  page: number,
  size: number,
  search: Record<string, unknown> = {},
): Promise<PageResult<T>> {
  const { data } = await api.get<ApiResponse<ApiPageResponse<T>>>(endpoint, {
    params: { page, size, ...search },
  });
  return { content: data.data.content, totalElements: data.data.totalElements };
}

export async function fetchDetail<T>(endpoint: string, id: string | number): Promise<T> {
  const { data } = await api.get<ApiResponse<T>>(`${endpoint}/${id}`);
  return data.data;
}

export async function createItem(
  endpoint: string,
  body: Record<string, unknown>,
): Promise<void> {
  await api.post(endpoint, body);
}

export async function updateItem(
  endpoint: string,
  id: string | number,
  body: Record<string, unknown>,
): Promise<void> {
  await api.patch(`${endpoint}/${id}`, body);
}

export async function deleteItem(endpoint: string, id: string | number): Promise<void> {
  await api.delete(`${endpoint}/${id}`);
}

export function getApiError(err: unknown, fallback: string): string {
  if (axios.isAxiosError(err)) {
    const msg = (err.response?.data as ApiResponse<unknown>)?.message;
    return msg ?? fallback;
  }
  return fallback;
}
