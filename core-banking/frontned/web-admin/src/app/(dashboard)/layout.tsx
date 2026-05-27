import { redirect } from 'next/navigation';
import { getServerAuthState } from '@/lib/authStore-server';
import { SidebarProvider, SidebarInset } from '@/components/ui/sidebar';
import { TooltipProvider } from '@/components/ui/tooltip';
import Sidebar from '@/components/layout/Sidebar';
import { CodeProvider } from '@/components/providers/CodeProvider';

export default async function DashboardLayout({ children }: { children: React.ReactNode }) {
  // 토큰 존재 + JWT exp 모두 검증.
  // - AUTHENTICATED   : access token 유효 → 그대로 진입
  // - REFRESH_ONLY    : access는 만료지만 refresh 유효 → 진입 후 axios interceptor가 자동 갱신
  // - UNAUTHENTICATED : 둘 다 없거나 만료 → 즉시 로그인 화면으로
  const authState = await getServerAuthState();
  if (authState === 'UNAUTHENTICATED') {
    redirect('/login');
  }

  return (
    <CodeProvider>
      <TooltipProvider delayDuration={0}>
        <SidebarProvider>
          <Sidebar />
          <SidebarInset>
            <main className="flex-1 p-6 overflow-auto">
              {children}
            </main>
          </SidebarInset>
        </SidebarProvider>
      </TooltipProvider>
    </CodeProvider>
  );
}
