import { cookies } from 'next/headers';
  const accessToken= 'accessToken';
  const refreshToken= 'refreshToken';

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
