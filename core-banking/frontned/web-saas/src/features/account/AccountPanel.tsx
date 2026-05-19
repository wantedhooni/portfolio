"use client";

import { useMemo, useState } from "react";
import { Search } from "lucide-react";

import { formatMoney } from "@/lib/format";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import type { Account } from "./account.types";

interface Props {
  accounts: Account[];
  selectedAccountId: number | null;
  onSelect: (id: number) => void;
}

/**
 * 계좌 목록을 검색·선택할 수 있는 패널입니다.
 */
export function AccountPanel({ accounts, selectedAccountId, onSelect }: Props) {
  const [keyword, setKeyword] = useState("");

  const filtered = useMemo(() => {
    const q = keyword.trim().toLowerCase();
    if (!q) return accounts;
    return accounts.filter((a) =>
      [a.accountName, a.accountNumber, a.currency].join(" ").toLowerCase().includes(q),
    );
  }, [accounts, keyword]);

  return (
    <section className="bank-panel" aria-label="계좌 현황">
      <div className="bank-panel-header">
        <div>
          <span>Accounts</span>
          <h2>계좌 현황</h2>
        </div>
        <div className="bank-search">
          <Search />
          <Input
            placeholder="계좌명, 번호, 통화 검색"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
        </div>
        <Separator className="bank-panel-separator" />
      </div>

      {accounts.length === 0 ? (
        <p className="bank-empty">연결된 계좌가 없습니다.</p>
      ) : (
        <div className="bank-account-list">
          {filtered.map((account) => (
            <button
              key={account.id}
              type="button"
              className="bank-account-row"
              data-active={account.id === selectedAccountId}
              onClick={() => onSelect(account.id)}
            >
              <span>
                <strong>{account.accountName}</strong>
                <small>{account.accountNumber}</small>
              </span>
              <span>
                <Badge variant={account.accountType === "REAL" ? "default" : "secondary"}>
                  {account.accountType}
                </Badge>
                <strong>{formatMoney(account.balance, account.currency)}</strong>
              </span>
            </button>
          ))}
        </div>
      )}
    </section>
  );
}
