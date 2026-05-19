"use client";

import { useWorkspaceContext } from "@/workspace/WorkspaceProvider";
import { PageHeader } from "@/workspace/PageHeader";
import { AccountPanel } from "@/features/account/AccountPanel";
import { CashPanel } from "@/features/account/CashPanel";

/**
 * 계좌 관리 페이지입니다.
 * 계좌 목록 조회·선택과 입출금 처리를 제공합니다.
 */
export default function AccountsPage() {
  const { accounts, selectedAccountId, selectAccount, deposit, withdraw, isLoading, error } =
    useWorkspaceContext();

  return (
    <>
      <PageHeader title="계좌 관리" />

      {error && <p className="bank-error">{error}</p>}

      <div className="bank-content-grid">
        <AccountPanel
          accounts={accounts}
          selectedAccountId={selectedAccountId}
          onSelect={(id) => void selectAccount(id)}
        />
        <CashPanel isLoading={isLoading} onDeposit={deposit} onWithdraw={withdraw} />
      </div>
    </>
  );
}
