'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { logout } from '@/features/auth/service';
import {
  ArrowLeftRightIcon,
  BookOpenIcon,
  BriefcaseIcon,
  CalendarRangeIcon,
  ClipboardListIcon,
  CoinsIcon,
  FileTextIcon,
  ClockIcon,
  HandshakeIcon,
  HeartHandshakeIcon,
  KeyRoundIcon,
  LayersIcon,
  LandmarkIcon,
  LayoutDashboardIcon,
  LineChartIcon,
  LogOutIcon,
  PackageIcon,
  ReceiptIcon,
  ReceiptTextIcon,
  RepeatIcon,
  ScaleIcon,
  ShieldCheckIcon,
  ShieldIcon,
  ShieldPlusIcon,
  TrendingUpIcon,
  UserRoundIcon,
} from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from '@/components/ui/sidebar';

interface NavItem {
  href: string;
  label: string;
  icon: LucideIcon;
}

interface NavGroup {
  label: string;
  items: NavItem[];
}

const NAV_GROUPS: NavGroup[] = [
  {
    label: '일반',
    items: [
      { href: '/dashboard',                     label: '대시보드',    icon: LayoutDashboardIcon },
      { href: '/dashboard/user',                label: '사용자 관리', icon: UserRoundIcon },
      { href: '/dashboard/admin',               label: '어드민 관리', icon: ShieldIcon },
    ],
  },
  {
    label: '계좌 · 거래',
    items: [
      { href: '/dashboard/account',             label: '계좌 관리',   icon: BriefcaseIcon },
      { href: '/dashboard/account/transfer',    label: '계좌이체',    icon: ArrowLeftRightIcon },
      { href: '/dashboard/account-transaction', label: '거래 내역',   icon: ReceiptIcon },
    ],
  },
  {
    label: '주식',
    items: [
      { href: '/dashboard/stock',         label: '종목 관리',  icon: TrendingUpIcon },
      { href: '/dashboard/order',         label: '주문 관리',  icon: ClipboardListIcon },
      { href: '/dashboard/order/place',   label: '주문 접수',  icon: ShieldPlusIcon },
      { href: '/dashboard/trade',         label: '체결 내역',  icon: ReceiptIcon },
      { href: '/dashboard/portfolio',     label: '포트폴리오', icon: LineChartIcon },
    ],
  },
  {
    label: '외환',
    items: [
      { href: '/dashboard/fx/currency',         label: '통화',        icon: CoinsIcon },
      { href: '/dashboard/fx/rate',             label: '환율',        icon: TrendingUpIcon },
      { href: '/dashboard/fx/conversion',       label: '환전',        icon: RepeatIcon },
    ],
  },
  {
    label: '보험',
    items: [
      { href: '/dashboard/insurance/product',   label: '보험 상품',   icon: PackageIcon },
      { href: '/dashboard/insurance/policy',    label: '보험 증권',   icon: ShieldCheckIcon },
      { href: '/dashboard/insurance/claim',     label: '보험금 청구', icon: HeartHandshakeIcon },
    ],
  },
  {
    label: '원장',
    items: [
      { href: '/dashboard/ledger/account',       label: '계정과목',   icon: BookOpenIcon },
      { href: '/dashboard/ledger/period',        label: '회계기간',   icon: CalendarRangeIcon },
      { href: '/dashboard/ledger/journal',       label: '분개',       icon: FileTextIcon },
      { href: '/dashboard/ledger/trial-balance', label: '시산표',     icon: ScaleIcon },
    ],
  },
  {
    label: '정산 · 청구',
    items: [
      { href: '/dashboard/settlement',        label: '정산 관리', icon: HandshakeIcon },
      { href: '/dashboard/billing/invoice',   label: '청구서 관리', icon: ReceiptTextIcon },
    ],
  },
  {
    label: '스케줄러',
    items: [
      { href: '/dashboard/scheduler/quartz', label: 'Quartz Job', icon: ClockIcon },
      { href: '/dashboard/scheduler/batch',  label: 'Spring Batch', icon: LayersIcon },
    ],
  },
  {
    label: 'RBAC',
    items: [
      { href: '/dashboard/rbac/role', label: '역할 관리', icon: KeyRoundIcon },
    ],
  },
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
            <LandmarkIcon className="size-4 text-primary-foreground" />
          </div>
          <span className="text-sm font-semibold">Revy Bank Admin</span>
        </div>
      </SidebarHeader>

      <SidebarContent>
        {NAV_GROUPS.map((group) => (
          <SidebarGroup key={group.label}>
            <SidebarGroupLabel>{group.label}</SidebarGroupLabel>
            <SidebarGroupContent>
              <SidebarMenu>
                {group.items.map(({ href, label, icon: Icon }) => (
                  <SidebarMenuItem key={href}>
                    <SidebarMenuButton
                      asChild
                      isActive={pathname === href || pathname.startsWith(href + '/')}
                      tooltip={label}
                    >
                      <Link href={href}>
                        <Icon />
                        <span>{label}</span>
                      </Link>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                ))}
              </SidebarMenu>
            </SidebarGroupContent>
          </SidebarGroup>
        ))}
      </SidebarContent>

      <SidebarFooter>
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton onClick={handleLogout} tooltip="로그아웃">
              <LogOutIcon />
              <span>로그아웃</span>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarFooter>
    </Sidebar>
  );
}
