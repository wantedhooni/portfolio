import { cookies } from 'next/headers';
import {COOKIE_NAME_ACCESS_TOKEN, COOKIE_NAME_REFRESH_TOKEN} from '@/constant/constants';
  const accessToken= COOKIE_NAME_ACCESS_TOKEN;
  const refreshToken= COOKIE_NAME_REFRESH_TOKEN;

export async function getServerTokens(): Promise<{
  accessToken: string | null;
  refreshToken: string | null;
}> {
  const store = await cookies();
  return {
    accessToken: store.get(accessToken)?.value ?? null,
    refreshToken: store.get(refreshToken)?.value ?? null,
  };
}
