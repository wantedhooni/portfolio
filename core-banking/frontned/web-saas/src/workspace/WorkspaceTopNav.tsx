"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { LogOut, RefreshCw } from "lucide-react";
import { toast } from "sonner";

import { useSession } from "./useSession";
import { useWorkspaceContext } from "./WorkspaceProvider";
import { Button } from "@/components/ui/button";

const NAV = [
  { href: "/workspace", label: "대시보드" },
  { href: "/workspace/accounts", label: "계좌" },
  { href: "/workspace/trades", label: "거래" },
  { href: "/workspace/stocks", label: "종목" },
] as const;

/**
 * 워크스페이스 상단 수평 네비게이션입니다.
 * 브랜드, 메뉴 링크, 계좌 선택, 새로고침, 로그아웃을 포함합니다.
 */
export function WorkspaceTopNav() {
  const pathname = usePathname();
  const router = useRouter();
  const { logout } = useSession();
  const { accounts, selectedAccountId, selectAccount, refresh, isLoading } =
    useWorkspaceContext();

  async function handleLogout() {
    await logout();
    toast.message("세션을 종료했습니다.");
    router.push("/login");
  }

  return (
    <header className="saas-topnav">
      <Link href="/workspace" className="saas-brand" aria-label="Revy Bank OS 홈">
        <div className="saas-brand-mark">RB</div>
        <strong>Revy Bank OS</strong>
      </Link>

      <nav className="saas-nav" aria-label="주요 메뉴">
        {NAV.map(({ href, label }) => (
          <Link key={href} href={href} data-active={pathname === href}>
            {label}
          </Link>
        ))}
      </nav>

      <div className="saas-topnav-right">
        {accounts.length > 0 && (
          <select
            className="bank-select"
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
        )}
        <Button
          variant="ghost"
          size="sm"
          disabled={isLoading}
          onClick={() => void refresh(selectedAccountId ?? undefined)}
          aria-label="새로고침"
        >
          <RefreshCw />
        </Button>
        <Button variant="outline" size="sm" onClick={handleLogout}>
          <LogOut data-icon="inline-start" />
          로그아웃
        </Button>
      </div>
    </header>
  );
}
