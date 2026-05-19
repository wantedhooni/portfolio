import { redirect } from 'next/navigation';
import { getServerTokens } from '@/lib/authStore-server';
import { SidebarProvider, SidebarInset } from '@/components/ui/sidebar';
import Sidebar from '@/components/layout/Sidebar';

export default async function DashboardLayout({ children }: { children: React.ReactNode }) {
  const { accessToken, refreshToken } = await getServerTokens();

  if (!accessToken && !refreshToken) {
    redirect('/login');
  }

  return (
    <SidebarProvider>
      <Sidebar />
      <SidebarInset>
        <main className="flex-1 p-6 overflow-auto">
          {children}
        </main>
      </SidebarInset>
    </SidebarProvider>
  );
}
