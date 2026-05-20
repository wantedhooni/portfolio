"use client";

import { useWorkspaceContext } from "@/workspace/WorkspaceProvider";
import { PageHeader } from "@/workspace/PageHeader";
import { AccountPanel } from "@/features/account/AccountPanel";
import { CashPanel } from "@/features/account/CashPanel";
import { PositionTable } from "@/features/portfolio/PositionTable";

/**
 * 계좌 관리 페이지입니다.
 * 계좌 목록·생성·수정·폐쇄, 입출금, 보유 포지션 상세를 제공합니다.
 */
export default function AccountsPage() {
  const {
    accounts,
    selectedAccountId,
    selectedAccount,
    positions,
    stocks,
    selectAccount,
    deposit,
    withdraw,
    createAccount,
    updateAccount,
    closeAccount,
    isLoading,
    error,
  } = useWorkspaceContext();

  const currency = selectedAccount?.currency ?? "KRW";

  return (
    <>
      <PageHeader title="계좌 관리" />

      {error && <p className="bank-error">{error}</p>}

      <div className="bank-content-grid">
        <AccountPanel
          accounts={accounts}
          selectedAccountId={selectedAccountId}
          onSelect={(id) => void selectAccount(id)}
          onCreateAccount={createAccount}
          onUpdateAccount={updateAccount}
          onCloseAccount={closeAccount}
        />
        <CashPanel isLoading={isLoading} onDeposit={deposit} onWithdraw={withdraw} />
      </div>

      <PositionTable positions={positions} stocks={stocks} currency={currency} />
    </>
  );
}
