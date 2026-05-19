'use client';

import { api } from '@/lib/api';
import type { ApiResponse } from '@/types';
import type { Portfolio } from './types';

export async function fetchPortfolio(accountId: number | string): Promise<Portfolio> {
  const { data } = await api.get<ApiResponse<Portfolio>>(`/api/v1/portfolio/${accountId}`);
  return data.data;
}
