"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { Banknote, BriefcaseBusiness, CircleDollarSign, LogOut, WalletCards } from "lucide-react";
import { toast } from "sonner";

import { useSession } from "./useSession";
import { Button } from "@/components/ui/button";

const NAV = [
  { href: "/workspace", Icon: WalletCards, label: "대시보드" },
  { href: "/workspace/accounts", Icon: Banknote, label: "계좌" },
  { href: "/workspace/trades", Icon: CircleDollarSign, label: "거래" },
  { href: "/workspace/stocks", Icon: BriefcaseBusiness, label: "종목" },
] as const;

/**
 * 워크스페이스 좌측 사이드바입니다.
 * 현재 경로를 기준으로 활성 메뉴를 강조하며, 로그아웃을 처리합니다.
 */
export function WorkspaceSidebar() {
  const pathname = usePathname();
  const router = useRouter();
  const { logout } = useSession();

  async function handleLogout() {
    await logout();
    toast.message("세션을 종료했습니다.");
    router.push("/login");
  }

  return (
    <aside className="bank-sidebar">
      <div className="bank-brand">
        <div className="bank-brand-mark">RB</div>
        <div>
          <strong>Revy Bank OS</strong>
          <span>Core Banking SaaS</span>
        </div>
      </div>

      <nav className="bank-nav" aria-label="주요 메뉴">
        {NAV.map(({ href, Icon, label }) => (
          <Link key={href} href={href} data-active={pathname === href}>
            <Icon />
            <span>{label}</span>
          </Link>
        ))}
      </nav>

      <div className="bank-sidebar-footer">
        <Button variant="outline" size="sm" onClick={handleLogout}>
          <LogOut data-icon="inline-start" />
          로그아웃
        </Button>
      </div>
    </aside>
  );
}
