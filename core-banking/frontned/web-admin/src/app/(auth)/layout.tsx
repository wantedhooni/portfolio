import { redirect } from 'next/navigation';
import { getServerAuthState } from '@/lib/authStore-server';

export default async function AuthLayout({ children }: { children: React.ReactNode }) {
  // 이미 인증 가능한 상태에서 /login 등으로 들어오면 즉시 대시보드로 보낸다.
  // (REFRESH_ONLY 도 포함 — 진입 후 클라이언트 interceptor가 자동 갱신)
  const authState = await getServerAuthState();
  if (authState !== 'UNAUTHENTICATED') {
    redirect('/dashboard');
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      {children}
    </div>
  );
}
