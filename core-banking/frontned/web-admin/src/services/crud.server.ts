import { getServerTokens } from '@/lib/authStore-server';
import { BASE_URL } from '@/lib/api';
import type { ApiResponse } from '@/types';

export async function fetchDetailServer<T>(endpoint: string, id: string | number): Promise<T> {
  const { accessToken } = await getServerTokens();

  const res = await fetch(`${BASE_URL}${endpoint}/${id}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
    },
    cache: 'no-store',
  });

  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  const json = (await res.json()) as ApiResponse<T>;
  return json.data;
}
