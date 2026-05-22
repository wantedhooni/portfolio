"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import {
  ArrowLeftRight,
  CreditCard,
  FileText,
  HeartHandshake,
  LayoutDashboard,
  LogOut,
  Moon,
  RefreshCw,
  Repeat,
  Search,
  Send,
  Sun,
} from "lucide-react";
import { useTheme } from "next-themes";
import { toast } from "sonner";

import { useSession } from "./useSession";
import { useWorkspaceContext } from "./WorkspaceProvider";
import { Button } from "@/components/ui/button";

const NAV = [
  { href: "/workspace", label: "대시보드", icon: LayoutDashboard },
  { href: "/workspace/accounts", label: "계좌 관리", icon: CreditCard },
  { href: "/workspace/transfer", label: "계좌이체", icon: Send },
  { href: "/workspace/fx", label: "환전", icon: Repeat },
  { href: "/workspace/insurance", label: "보험", icon: HeartHandshake },
  { href: "/workspace/billing", label: "청구서", icon: FileText },
  { href: "/workspace/trades", label: "거래 처리", icon: ArrowLeftRight },
  { href: "/workspace/stocks", label: "종목 조회", icon: Search },
] as const;

const AUTO_REFRESH_OPTIONS = [
  { label: "자동갱신 끔", value: 0 },
  { label: "30초", value: 30_000 },
  { label: "1분", value: 60_000 },
  { label: "5분", value: 300_000 },
];

/**
 * 워크스페이스 좌측 사이드바입니다.
 * 사용자 프로필, 메뉴, 계좌 선택, 다크모드, 자동갱신, 로그아웃을 포함합니다.
 */
export function WorkspaceSidebar() {
  const pathname = usePathname();
  const router = useRouter();
  const { logout } = useSession();
  const {
    accounts,
    selectedAccountId,
    selectAccount,
    refresh,
    isLoading,
    user,
    autoRefreshInterval,
    setAutoRefreshInterval,
  } = useWorkspaceContext();

  const { resolvedTheme, setTheme } = useTheme();
  const [mounted, setMounted] = useState(false);
  useEffect(() => setMounted(true), []);
  const isDark = mounted && resolvedTheme === "dark";

  async function handleLogout() {
    await logout();
    toast.message("세션을 종료했습니다.");
    router.push("/login");
  }

  const userInitials = user?.name
    ? user.name
        .split(" ")
        .map((w: string) => w[0])
        .join("")
        .toUpperCase()
        .slice(0, 2)
    : "—";

  return (
    <aside className="saas-sidebar">
      <Link href="/workspace" className="saas-brand" aria-label="Revy Bank OS 홈">
        <div className="saas-brand-mark">RB</div>
        <strong className="saas-brand-name">Revy Bank OS</strong>
      </Link>

      <nav className="saas-sidebar-nav" aria-label="주요 메뉴">
        {NAV.map(({ href, label, icon: Icon }) => (
          <Link key={href} href={href} data-active={pathname === href}>
            <Icon />
            <span>{label}</span>
          </Link>
        ))}
      </nav>

      <div className="saas-sidebar-footer">
        {/* 사용자 프로필 */}
        {user && (
          <div className="saas-user-profile">
            <div className="saas-user-avatar">{userInitials}</div>
            <div className="saas-user-info">
              <strong>{user.name as string}</strong>
              <span>{user.email as string}</span>
            </div>
          </div>
        )}

        {/* 계좌 선택 */}
        {accounts.length > 0 && (
          <div className="saas-account-select-wrap">
            <span className="saas-sidebar-micro-label">활성 계좌</span>
            <select
              className="bank-select saas-sidebar-select"
              value={selectedAccountId ?? ""}
              onChange={(e) => void selectAccount(Number(e.target.value))}
              aria-label="계좌 선택"
            >
              {accounts.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.accountName}
                </option>
              ))}
            </select>
          </div>
        )}

        {/* 자동갱신 */}
        <div className="saas-account-select-wrap">
          <span className="saas-sidebar-micro-label">자동갱신</span>
          <select
            className="bank-select saas-sidebar-select"
            value={autoRefreshInterval}
            onChange={(e) => setAutoRefreshInterval(Number(e.target.value))}
            aria-label="자동갱신 주기"
          >
            {AUTO_REFRESH_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>

        <div className="saas-sidebar-actions">
          {/* 다크모드 토글 */}
          {mounted && (
            <Button
              variant="ghost"
              size="sm"
              className="saas-sidebar-btn"
              onClick={() => setTheme(isDark ? "light" : "dark")}
            >
              {isDark ? <Sun /> : <Moon />}
              <span>{isDark ? "라이트 모드" : "다크 모드"}</span>
            </Button>
          )}

          <Button
            variant="ghost"
            size="sm"
            disabled={isLoading}
            onClick={() => void refresh(selectedAccountId ?? undefined)}
            className="saas-sidebar-btn"
          >
            <RefreshCw />
            <span>새로고침</span>
          </Button>
          <Button variant="ghost" size="sm" onClick={handleLogout} className="saas-sidebar-btn">
            <LogOut />
            <span>로그아웃</span>
          </Button>
        </div>
      </div>
    </aside>
  );
}
