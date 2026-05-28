"use client";

import { type FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import { ArrowRight, Info, Repeat, RefreshCw } from "lucide-react";
import { toast } from "sonner";

import { useWorkspaceContext } from "@/workspace/WorkspaceProvider";
import { PageHeader } from "@/workspace/PageHeader";
import { fxService } from "@/features/fx/fx.service";
import type {
  Currency,
  ExchangeRate,
  FxConversion,
  FxCorridor,
  RateType,
} from "@/features/fx/fx.types";
import { formatMoney, formatNumber, makeReference } from "@/lib/format";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";

const RATE_TYPES: RateType[] = ["SELL", "BUY", "MID", "CASH_BUY", "CASH_SELL", "REMIT_BUY", "REMIT_SELL"];

/**
 * 환전 페이지입니다.
 * 본인 계좌 간 통화 환전을 처리하고, 활성 통화·현재 환율·통화쌍 한도를 미리 보여줍니다.
 */
export default function FxPage() {
  const { accounts, refresh, isLoading } = useWorkspaceContext();

  const [currencies, setCurrencies]   = useState<Currency[]>([]);
  const [fromAccountId, setFromAccountId] = useState<number | null>(null);
  const [toAccountId, setToAccountId]     = useState<number | null>(null);
  const [fromAmount, setFromAmount]       = useState("100");
  const [rateType, setRateType]           = useState<RateType>("SELL");
  const [fee, setFee]                     = useState("0");
  const [referenceId, setReferenceId]     = useState(makeReference("FX"));

  const [rate, setRate]               = useState<ExchangeRate | null>(null);
  const [corridor, setCorridor]       = useState<FxCorridor | null>(null);
  const [rateLoading, setRateLoading] = useState(false);
  const [submitting, setSubmitting]   = useState(false);
  const [lastResult, setLastResult]   = useState<FxConversion | null>(null);

  const fromAccount = accounts.find((a) => a.id === fromAccountId) ?? null;
  const toAccount   = accounts.find((a) => a.id === toAccountId) ?? null;

  // 초기 통화 목록 로딩
  useEffect(() => {
    fxService.listCurrencies().then(setCurrencies).catch(() => toast.error("통화 목록 조회 실패"));
  }, []);

  // from/to 통화가 정해지면 환율 + 코리더 자동 조회
  const lookupRate = useCallback(async () => {
    if (!fromAccount || !toAccount) return;
    if (fromAccount.currency === toAccount.currency) {
      setRate(null);
      setCorridor(null);
      return;
    }
    setRateLoading(true);
    try {
      const [rateResult, corridorResult] = await Promise.allSettled([
        fxService.currentRate(fromAccount.currency, toAccount.currency, rateType),
        fxService.findCorridor(fromAccount.currency, toAccount.currency),
      ]);

      setRate(rateResult.status === "fulfilled" ? rateResult.value : null);
      setCorridor(corridorResult.status === "fulfilled" ? corridorResult.value : null);

      if (rateResult.status === "rejected") toast.error("환율 조회 실패");
    } finally {
      setRateLoading(false);
    }
  }, [fromAccount, toAccount, rateType]);

  useEffect(() => { void lookupRate(); }, [lookupRate]);

  // 예상 환산 금액
  const estimatedTo = useMemo(() => {
    const amt = Number(fromAmount);
    const feeAmt = Number(fee || "0");
    if (!rate || !Number.isFinite(amt) || amt <= 0) return 0;
    return Math.max(0, amt * rate.rate - feeAmt);
  }, [rate, fromAmount, fee]);

  // 한도 검증
  const amt = Number(fromAmount);
  const belowMin = corridor && Number.isFinite(amt) && amt > 0 && amt < corridor.minAmount;
  const aboveMax = corridor?.maxAmount != null && Number.isFinite(amt) && amt > corridor.maxAmount;

  const sameAccount  = fromAccount && toAccount && fromAccount.id === toAccount.id;
  const sameCurrency = fromAccount && toAccount && fromAccount.currency === toAccount.currency;
  const insufficient = fromAccount && Number(fromAmount) > fromAccount.availableBalance;

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!fromAccount || !toAccount) return toast.error("출금/입금 계좌를 선택하세요.");
    if (sameAccount)                return toast.error("동일 계좌입니다.");
    if (sameCurrency)               return toast.error("동일 통화는 계좌이체를 이용하세요.");
    if (!Number.isFinite(amt) || amt <= 0) return toast.error("0보다 큰 금액을 입력하세요.");
    if (belowMin) return toast.error(`최소 환전 금액은 ${formatNumber(corridor!.minAmount)} ${fromAccount.currency} 입니다.`);
    if (aboveMax) return toast.error(`최대 환전 금액은 ${formatNumber(corridor!.maxAmount!)} ${fromAccount.currency} 입니다.`);

    setSubmitting(true);
    try {
      const result = await fxService.convert({
        fromAccountId: fromAccount.id,
        toAccountId:   toAccount.id,
        fromCurrencyCode: fromAccount.currency,
        toCurrencyCode:   toAccount.currency,
        fromAmount:    amt,
        rateType,
        fee:           Number(fee || "0"),
        referenceId:   referenceId || makeReference("FX"),
      });
      setLastResult(result);
      toast.success("환전이 완료되었습니다.");
      setReferenceId(makeReference("FX"));
      await refresh();
    } catch (err) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      toast.error(msg ?? "환전에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <PageHeader title="환전" />

      <div className="bank-content-grid">
        {/* 좌측: 환전 폼 */}
        <section className="bank-panel" aria-label="환전 정보">
          <div className="bank-panel-header">
            <div>
              <span>FX</span>
              <h2>환전 정보</h2>
            </div>
            <Separator className="bank-panel-separator" />
          </div>

          <form className="bank-form" onSubmit={handleSubmit}>
            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="from-acc">출금 계좌</FieldLabel>
                <select
                  id="from-acc"
                  className="bank-select"
                  value={fromAccountId ?? ""}
                  onChange={(e) => setFromAccountId(Number(e.target.value))}
                >
                  <option value="">— 선택 —</option>
                  {accounts.map((a) => (
                    <option key={a.id} value={a.id}>
                      {a.accountName} · {a.currency}
                    </option>
                  ))}
                </select>
                {fromAccount && (
                  <p className="text-xs text-muted-foreground">
                    가용잔고: {formatMoney(fromAccount.availableBalance, fromAccount.currency)}
                  </p>
                )}
              </Field>
              <Field>
                <FieldLabel htmlFor="to-acc">입금 계좌</FieldLabel>
                <select
                  id="to-acc"
                  className="bank-select"
                  value={toAccountId ?? ""}
                  onChange={(e) => setToAccountId(Number(e.target.value))}
                >
                  <option value="">— 선택 —</option>
                  {accounts.map((a) => (
                    <option key={a.id} value={a.id}>
                      {a.accountName} · {a.currency}
                    </option>
                  ))}
                </select>
                {toAccount && (
                  <p className="text-xs text-muted-foreground">
                    현재잔고: {formatMoney(toAccount.balance, toAccount.currency)}
                  </p>
                )}
              </Field>
            </FieldGroup>

            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="from-amount">
                  출금 금액 {fromAccount && <span className="text-muted-foreground text-xs">({fromAccount.currency})</span>}
                </FieldLabel>
                <Input
                  id="from-amount"
                  inputMode="decimal"
                  value={fromAmount}
                  onChange={(e) => setFromAmount(e.target.value)}
                />
                {corridor && (
                  <p className="text-xs text-muted-foreground">
                    최소 {formatNumber(corridor.minAmount)}
                    {corridor.maxAmount != null && ` · 최대 ${formatNumber(corridor.maxAmount)}`}
                  </p>
                )}
              </Field>
              <Field>
                <FieldLabel htmlFor="rate-type">환율 타입</FieldLabel>
                <select
                  id="rate-type"
                  className="bank-select"
                  value={rateType}
                  onChange={(e) => setRateType(e.target.value as RateType)}
                >
                  {RATE_TYPES.map((t) => (
                    <option key={t} value={t}>{t}</option>
                  ))}
                </select>
              </Field>
            </FieldGroup>

            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="fx-fee">
                  수수료 {toAccount && <span className="text-muted-foreground text-xs">({toAccount.currency})</span>}
                </FieldLabel>
                <Input
                  id="fx-fee"
                  inputMode="decimal"
                  value={fee}
                  onChange={(e) => setFee(e.target.value)}
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="fx-ref">참조번호</FieldLabel>
                <Input id="fx-ref" value={referenceId} onChange={(e) => setReferenceId(e.target.value)} />
              </Field>
            </FieldGroup>

            {/* 검증 알림 */}
            {sameAccount && (
              <p className="text-sm text-destructive">동일 계좌로 환전할 수 없습니다.</p>
            )}
            {sameCurrency && (
              <p className="text-sm text-destructive">통화가 동일합니다. 계좌이체 메뉴를 이용하세요.</p>
            )}
            {insufficient && (
              <p className="text-sm text-destructive">가용잔고가 부족합니다.</p>
            )}
            {belowMin && (
              <p className="text-sm text-destructive">
                최소 환전 금액은 {formatNumber(corridor!.minAmount)} {fromAccount?.currency} 입니다.
              </p>
            )}
            {aboveMax && (
              <p className="text-sm text-destructive">
                최대 환전 금액은 {formatNumber(corridor!.maxAmount!)} {fromAccount?.currency} 입니다.
              </p>
            )}

            <Button type="submit" disabled={submitting || isLoading} size="lg">
              <Repeat data-icon="inline-start" />
              {submitting ? "환전 처리 중..." : "환전 실행"}
            </Button>
          </form>
        </section>

        {/* 우측: 환율 + 코리더 + 미리보기 + 결과 */}
        <section className="bank-panel" aria-label="환율 정보">
          <div className="bank-panel-header">
            <div>
              <span>Live Rate</span>
              <h2>환율 · 미리보기</h2>
            </div>
            <Button size="sm" variant="ghost" onClick={lookupRate} disabled={rateLoading || !fromAccount || !toAccount}>
              <RefreshCw className={rateLoading ? "animate-spin" : ""} />
            </Button>
          </div>

          {/* 환율 카드 */}
          <div className="rounded-lg border bg-card/50 p-4 space-y-2">
            {rate ? (
              <>
                <div className="flex items-center justify-between">
                  <span className="text-xs uppercase text-muted-foreground">시세</span>
                  <Badge variant="outline">{rate.rateType}</Badge>
                </div>
                <div className="text-center text-2xl font-bold tabular-nums">
                  1 {rate.baseCurrencyCode} = {formatNumber(rate.rate)} {rate.quoteCurrencyCode}
                </div>
                <div className="flex items-center justify-between text-xs text-muted-foreground">
                  <span>{rate.source}</span>
                  <span>{new Date(rate.quotedAt).toLocaleString("ko-KR")}</span>
                </div>
              </>
            ) : (
              <p className="text-sm text-muted-foreground text-center py-6">
                {sameCurrency ? "동일 통화입니다." : "통화를 선택하면 환율이 표시됩니다."}
              </p>
            )}
          </div>

          {/* 코리더 정보 */}
          {corridor && (
            <div className="rounded-lg border bg-muted/20 p-3 mt-3 space-y-1.5">
              <div className="flex items-center gap-1.5 text-xs uppercase text-muted-foreground">
                <Info className="size-3" />
                통화쌍 한도
              </div>
              <div className="grid grid-cols-2 gap-x-4 gap-y-1 text-xs">
                <span className="text-muted-foreground">스프레드</span>
                <span className="tabular-nums font-medium">
                  {(corridor.spreadRate * 100).toFixed(2)}%
                </span>
                <span className="text-muted-foreground">최소 금액</span>
                <span className="tabular-nums">{formatNumber(corridor.minAmount)}</span>
                {corridor.maxAmount != null && (
                  <>
                    <span className="text-muted-foreground">최대 금액</span>
                    <span className="tabular-nums">{formatNumber(corridor.maxAmount)}</span>
                  </>
                )}
                {corridor.dailyLimit != null && (
                  <>
                    <span className="text-muted-foreground">일 한도</span>
                    <span className="tabular-nums">{formatNumber(corridor.dailyLimit)}</span>
                  </>
                )}
              </div>
            </div>
          )}

          {/* 미리보기 */}
          {fromAccount && toAccount && rate && !sameCurrency && (
            <div className="rounded-lg border bg-muted/30 p-4 mt-3 space-y-3">
              <div className="text-xs uppercase text-muted-foreground">예상 환산 결과</div>
              <div className="flex items-center justify-between gap-3">
                <div className="text-center sm:text-left">
                  <div className="text-xs text-muted-foreground">출금</div>
                  <div className="text-lg font-bold tabular-nums">
                    {formatMoney(Number(fromAmount) || 0, fromAccount.currency)}
                  </div>
                </div>
                <ArrowRight className="size-5 shrink-0 text-muted-foreground" />
                <div className="text-center sm:text-right">
                  <div className="text-xs text-muted-foreground">입금 (수수료 차감 후)</div>
                  <div className="text-lg font-bold tabular-nums text-primary">
                    {formatMoney(estimatedTo, toAccount.currency)}
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* 최근 결과 */}
          {lastResult && (
            <div className="rounded-lg border bg-primary/5 p-4 mt-3 space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-xs uppercase text-muted-foreground">최근 환전</span>
                <Badge>{lastResult.status}</Badge>
              </div>
              <div className="font-mono text-xs">{lastResult.conversionNumber}</div>
              <div className="text-sm tabular-nums">
                {formatMoney(lastResult.fromAmount, lastResult.fromCurrencyCode)}
                {" → "}
                <strong>{formatMoney(lastResult.toAmount, lastResult.toCurrencyCode)}</strong>
              </div>
              <div className="text-xs text-muted-foreground">
                적용환율 {formatNumber(lastResult.appliedRate)} · 수수료 {formatNumber(lastResult.fee)}
              </div>
            </div>
          )}
        </section>
      </div>
    </>
  );
}
