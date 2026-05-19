"use client";

import Link from "next/link";
import { ArrowRight, Banknote, BriefcaseBusiness, CircleDollarSign, TrendingUp } from "lucide-react";
import type { LucideIcon } from "lucide-react";

import { useWorkspaceContext } from "@/workspace/WorkspaceProvider";
import { LedgerPanel } from "@/features/trade/LedgerPanel";
import { formatMoney } from "@/lib/format";

/**
 * 워크스페이스 메인 대시보드 페이지입니다.
 * 총 자산 히어로, 핵심 지표, 빠른 실행 카드, 최근 거래내역을 표시합니다.
 */
export default function DashboardPage() {
  const { portfolio, transactions, stocks, selectedAccount, error } = useWorkspaceContext();
  const currency = selectedAccount?.currency ?? "KRW";

  const totalAsset = portfolio.balance + portfolio.totalUnrealizedPnl;
  const totalPnl = portfolio.totalRealizedPnl + portfolio.totalUnrealizedPnl;

  return (
    <>
      {error && <p className="bank-error">{error}</p>}

      <div className="saas-hero">
        <div>
          <p className="saas-hero-label">총 자산</p>
          <p className="saas-hero-balance">{formatMoney(totalAsset, currency)}</p>
          <p className="saas-hero-sub">{selectedAccount?.accountName ?? "— 계좌를 선택하세요"}</p>
        </div>
        <span className="saas-hero-badge" data-tone={totalPnl >= 0 ? "positive" : "negative"}>
          {totalPnl >= 0 ? "+" : ""}
          {formatMoney(totalPnl, currency)}
        </span>
      </div>

      <div className="saas-stats">
        <div className="saas-stat">
          <span className="saas-stat-label">현금 잔고</span>
          <strong className="saas-stat-value">{formatMoney(portfolio.balance, currency)}</strong>
        </div>
        <div className="saas-stat">
          <span className="saas-stat-label">가용 잔고</span>
          <strong className="saas-stat-value">
            {formatMoney(portfolio.availableBalance, currency)}
          </strong>
        </div>
        <div
          className="saas-stat"
          data-tone={portfolio.totalUnrealizedPnl >= 0 ? "positive" : "negative"}
        >
          <span className="saas-stat-label">미실현 손익</span>
          <strong className="saas-stat-value">
            {formatMoney(portfolio.totalUnrealizedPnl, currency)}
          </strong>
        </div>
        <div
          className="saas-stat"
          data-tone={portfolio.totalRealizedPnl >= 0 ? "positive" : "negative"}
        >
          <span className="saas-stat-label">실현 손익</span>
          <strong className="saas-stat-value">
            {formatMoney(portfolio.totalRealizedPnl, currency)}
          </strong>
        </div>
      </div>

      <div>
        <p className="saas-section-label">빠른 실행</p>
        <div className="saas-action-grid">
          <ActionCard
            href="/workspace/accounts"
            icon={Banknote}
            title="계좌 입출금"
            desc="잔고 확인 및 현금 이동"
          />
          <ActionCard
            href="/workspace/trades"
            icon={CircleDollarSign}
            title="주식 매매"
            desc="매수 · 매도 · 배당 처리"
          />
          <ActionCard
            href="/workspace/stocks"
            icon={BriefcaseBusiness}
            title="종목 검색"
            desc="등록 종목 조회 및 시세"
          />
          <ActionCard
            href="/workspace/trades"
            icon={TrendingUp}
            title="포트폴리오"
            desc="보유 포지션 및 손익 현황"
          />
        </div>
      </div>

      <LedgerPanel transactions={transactions} stocks={stocks} currency={currency} limit={5} />
    </>
  );
}

function ActionCard({
  href,
  icon: Icon,
  title,
  desc,
}: {
  href: string;
  icon: LucideIcon;
  title: string;
  desc: string;
}) {
  return (
    <Link href={href} className="saas-action-card">
      <div className="saas-action-card-icon">
        <Icon />
      </div>
      <p className="saas-action-card-title">{title}</p>
      <p className="saas-action-card-desc">{desc}</p>
      <ArrowRight className="saas-action-card-arrow" />
    </Link>
  );
}
