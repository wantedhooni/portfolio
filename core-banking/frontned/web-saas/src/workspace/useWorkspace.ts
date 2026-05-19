"use client";

import { startTransition, useCallback, useEffect, useState } from "react";

import { accountService } from "@/features/account/account.service";
import type { Account, MoneyMoveRequest } from "@/features/account/account.types";
import { tradeService } from "@/features/trade/trade.service";
import type { AccountTransaction, DividendRequest, TradeRequest } from "@/features/trade/trade.types";
import { stockService } from "@/features/stock/stock.service";
import type { Stock } from "@/features/stock/stock.types";
import { portfolioService } from "@/features/portfolio/portfolio.service";
import type { Portfolio, Position } from "@/features/portfolio/portfolio.types";
import type { ApiPageResponse } from "@/shared/types/api.types";

const EMPTY_PORTFOLIO: Portfolio = {
  accountId: 0,
  balance: 0,
  availableBalance: 0,
  totalRealizedPnl: 0,
  totalUnrealizedPnl: 0,
  positions: [],
};

function unwrapPage<T>(payload: ApiPageResponse<T> | T[]): T[] {
  return Array.isArray(payload) ? payload : payload.content;
}

/**
 * 워크스페이스 전체 데이터 상태를 관리하는 훅입니다.
 *
 * 계좌 목록, 선택 계좌, 포트폴리오, 포지션, 거래내역, 종목 목록을
 * API에서 불러오고 입출금 / 매매 뮤테이션을 제공합니다.
 */
export function useWorkspace() {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [stocks, setStocks] = useState<Stock[]>([]);
  const [portfolio, setPortfolio] = useState<Portfolio>(EMPTY_PORTFOLIO);
  const [positions, setPositions] = useState<Position[]>([]);
  const [transactions, setTransactions] = useState<AccountTransaction[]>([]);
  const [selectedAccountId, setSelectedAccountId] = useState<number | null>(null);

  const selectedAccount = accounts.find((a) => a.id === selectedAccountId) ?? null;

  /**
   * 특정 계좌의 포트폴리오, 거래내역, 포지션을 한 번에 갱신합니다.
   */
  const loadForAccount = useCallback(async (accountId: number) => {
    const [portfolio, txResult, positions] = await Promise.all([
      portfolioService.getPortfolio(accountId),
      tradeService.listTransactions(accountId, {}, { page: 0, size: 30 }),
      portfolioService.listPositions(accountId),
    ]);
    setPortfolio(portfolio);
    setTransactions(unwrapPage(txResult));
    setPositions(positions);
  }, []);

  /**
   * 계좌 목록과 종목 목록을 새로 불러옵니다.
   * keepAccountId가 목록 안에 있으면 해당 계좌를 선택 상태로 유지합니다.
   */
  const refresh = useCallback(
    async (keepAccountId?: number) => {
      setIsLoading(true);
      setError(null);
      try {
        const [accountResult, stockResult] = await Promise.all([
          accountService.listAccounts({}, { page: 0, size: 50 }),
          stockService.listStocks({}, { page: 0, size: 100 }),
        ]);

        const accountList = unwrapPage(accountResult);
        setAccounts(accountList);
        setStocks(unwrapPage(stockResult));

        if (accountList.length === 0) {
          setPortfolio(EMPTY_PORTFOLIO);
          setPositions([]);
          setTransactions([]);
          return;
        }

        const targetId = accountList.some((a) => a.id === keepAccountId)
          ? keepAccountId!
          : accountList[0].id;

        setSelectedAccountId(targetId);
        await loadForAccount(targetId);
      } catch (err) {
        setError("백엔드 서버에 연결할 수 없습니다. 서버 상태를 확인하세요.");
        console.error(err);
      } finally {
        setIsLoading(false);
      }
    },
    [loadForAccount],
  );

  useEffect(() => {
    startTransition(() => {
      void refresh();
    });
  }, [refresh]);

  /**
   * 계좌를 선택하고 해당 계좌의 데이터를 즉시 갱신합니다.
   */
  const selectAccount = useCallback(
    async (id: number) => {
      setSelectedAccountId(id);
      setIsLoading(true);
      try {
        await loadForAccount(id);
      } catch (err) {
        setError("계좌 데이터를 불러오지 못했습니다.");
        console.error(err);
      } finally {
        setIsLoading(false);
      }
    },
    [loadForAccount],
  );

  /**
   * 입금 처리 후 계좌 데이터를 갱신합니다.
   */
  const deposit = useCallback(
    async (payload: MoneyMoveRequest) => {
      if (selectedAccountId == null) return;
      await accountService.deposit(selectedAccountId, payload);
      await loadForAccount(selectedAccountId);
    },
    [selectedAccountId, loadForAccount],
  );

  /**
   * 출금 처리 후 계좌 데이터를 갱신합니다.
   */
  const withdraw = useCallback(
    async (payload: MoneyMoveRequest) => {
      if (selectedAccountId == null) return;
      await accountService.withdraw(selectedAccountId, payload);
      await loadForAccount(selectedAccountId);
    },
    [selectedAccountId, loadForAccount],
  );

  /**
   * 매수 거래 후 계좌 데이터를 갱신합니다.
   */
  const buy = useCallback(
    async (payload: TradeRequest) => {
      if (selectedAccountId == null) return;
      await tradeService.buy(selectedAccountId, payload);
      await loadForAccount(selectedAccountId);
    },
    [selectedAccountId, loadForAccount],
  );

  /**
   * 매도 거래 후 계좌 데이터를 갱신합니다.
   */
  const sell = useCallback(
    async (payload: TradeRequest) => {
      if (selectedAccountId == null) return;
      await tradeService.sell(selectedAccountId, payload);
      await loadForAccount(selectedAccountId);
    },
    [selectedAccountId, loadForAccount],
  );

  /**
   * 배당 입금 후 계좌 데이터를 갱신합니다.
   */
  const dividend = useCallback(
    async (payload: DividendRequest) => {
      if (selectedAccountId == null) return;
      await tradeService.dividend(selectedAccountId, payload);
      await loadForAccount(selectedAccountId);
    },
    [selectedAccountId, loadForAccount],
  );

  return {
    isLoading,
    error,
    accounts,
    stocks,
    portfolio,
    positions,
    transactions,
    selectedAccount,
    selectedAccountId,
    selectAccount,
    refresh,
    deposit,
    withdraw,
    buy,
    sell,
    dividend,
  };
}
