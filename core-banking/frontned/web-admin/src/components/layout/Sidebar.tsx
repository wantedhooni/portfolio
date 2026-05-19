'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { logout } from '@/features/auth/service';
import {
  LayoutDashboardIcon,
  LogOutIcon,
  ReceiptIcon,
  ShieldIcon,
  UserRoundIcon

} from 'lucide-react';
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from '@/components/ui/sidebar';

const NAV_ITEMS = [
  { href: '/dashboard',                    label: '대시보드',    icon: LayoutDashboardIcon },
  { href: '/dashboard/user',            label: '사용자 관리',   icon: UserRoundIcon },
  { href: '/dashboard/account',            label: '계정 관리',   icon: ReceiptIcon },
  { href: '/dashboard/account-transaction',label: '거래 내역',   icon: ReceiptIcon },
  { href: '/dashboard/admin',              label: '어드민 관리', icon: ShieldIcon },
];

export default function AppSidebar() {
  const pathname = usePathname();
  const router = useRouter();

  async function handleLogout() {
    await logout();
    router.replace('/login');
  }

  return (
    <Sidebar>
      <SidebarHeader>
        <div className="flex items-center gap-2.5 px-2 py-1">
          <div className="size-7 rounded-lg bg-primary flex items-center justify-center shrink-0">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <rect x="1" y="1" width="5" height="5" rx="1" fill="white" fillOpacity="0.9"/>
              <rect x="8" y="1" width="5" height="5" rx="1" fill="white" fillOpacity="0.6"/>
              <rect x="1" y="8" width="5" height="5" rx="1" fill="white" fillOpacity="0.6"/>
              <rect x="8" y="8" width="5" height="5" rx="1" fill="white" fillOpacity="0.9"/>
            </svg>
          </div>
          <span className="text-sm font-semibold">Admin Panel</span>
        </div>
      </SidebarHeader>

      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupContent>
            <SidebarMenu>
              {NAV_ITEMS.map(({ href, label, icon: Icon }) => (
                <SidebarMenuItem key={href}>
                  <SidebarMenuButton asChild isActive={pathname === href}>
                    <Link href={href}>
                      <Icon />
                      {label}
                    </Link>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>

      <SidebarFooter>
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton onClick={handleLogout}>
              <LogOutIcon />
              로그아웃
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarFooter>
    </Sidebar>
  );
}
