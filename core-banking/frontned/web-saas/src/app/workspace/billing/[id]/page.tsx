"use client";

import { use, useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowLeft, BanknoteArrowDown, Receipt } from "lucide-react";
import { toast } from "sonner";

import { useWorkspaceContext } from "@/workspace/WorkspaceProvider";
import { PageHeader } from "@/workspace/PageHeader";
import { billingService } from "@/features/billing/billing.service";
import type { Invoice, InvoiceStatus } from "@/features/billing/billing.types";
import { formatMoney } from "@/lib/format";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";

const STATUS_VARIANT: Record<InvoiceStatus, "default" | "outline" | "destructive" | "secondary"> = {
  DRAFT:     "outline",
  ISSUED:    "secondary",
  PAID:      "default",
  OVERDUE:   "destructive",
  CANCELLED: "secondary",
};

const STATUS_LABEL: Record<InvoiceStatus, string> = {
  DRAFT:     "작성중",
  ISSUED:    "발행됨",
  PAID:      "납부완료",
  OVERDUE:   "기한초과",
  CANCELLED: "취소됨",
};

const ITEM_TYPE_LABEL: Record<string, string> = {
  ACCOUNT_FEE:       "계좌 유지수수료",
  TRADE_COMMISSION:  "주식 체결수수료",
  FX_SPREAD_FEE:     "환전 스프레드",
  TRANSFER_FEE:      "이체 수수료",
  INSURANCE_PREMIUM: "보험료",
  SERVICE_FEE:       "서비스 이용료",
};

/**
 * 청구서 상세 페이지.
 * 본인 청구서에 한해 항목 명세 + 납부 가능.
 */
export default function InvoiceDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const { accounts } = useWorkspaceContext();
  const [invoice, setInvoice] = useState<Invoice | null>(null);
  const [loading, setLoading] = useState(true);
  const [paying, setPaying] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setInvoice(await billingService.getInvoice(Number(id)));
    } catch {
      toast.error("청구서를 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { void load(); }, [load]);

  async function handlePay() {
    if (!invoice) return;
    if (!confirm(`총 ${formatMoney(invoice.totalAmount, invoice.currency)}을(를) 납부하시겠습니까?`)) return;
    setPaying(true);
    try {
      await billingService.pay(invoice.id);
      toast.success("납부가 완료되었습니다.");
      await load();
    } catch (e) {
      toast.error("납부 처리 실패");
      console.error(e);
    } finally {
      setPaying(false);
    }
  }

  if (loading) {
    return (
      <>
        <PageHeader title="청구서 상세" />
        <p className="text-sm text-muted-foreground">로딩 중…</p>
      </>
    );
  }
  if (!invoice) {
    return (
      <>
        <PageHeader title="청구서 상세" />
        <p className="text-sm text-muted-foreground">청구서를 찾을 수 없습니다.</p>
      </>
    );
  }

  const account = accounts.find((a) => a.id === invoice.accountId);
  const payable = invoice.status === "ISSUED" || invoice.status === "OVERDUE";

  return (
    <>
      <PageHeader title="청구서 상세" />

      <div className="flex items-center gap-3 mb-4 flex-wrap">
        <Button variant="outline" size="sm" onClick={() => router.push("/workspace/billing")}>
          <ArrowLeft data-icon="inline-start" />
          목록
        </Button>
        <div className="flex-1 min-w-0">
          <h2 className="text-xl font-semibold">청구서 #{invoice.id}</h2>
          <p className="text-sm text-muted-foreground">
            {invoice.billingPeriod}
            {account && ` · ${account.accountName} (${account.accountNumber})`}
          </p>
        </div>
        <Badge variant={STATUS_VARIANT[invoice.status]}>{STATUS_LABEL[invoice.status]}</Badge>
        {payable && (
          <Button onClick={handlePay} disabled={paying}>
            <BanknoteArrowDown data-icon="inline-start" />
            {paying ? "처리 중…" : "납부하기"}
          </Button>
        )}
      </div>

      <div className="grid gap-4 xl:grid-cols-[1.5fr_1fr]">
        {/* 항목 명세 */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base flex items-center gap-2">
              <Receipt className="size-4" />
              항목 명세 ({invoice.items.length})
            </CardTitle>
          </CardHeader>
          <CardContent>
            {invoice.items.length === 0 ? (
              <p className="text-sm text-muted-foreground">항목이 없습니다.</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b text-xs uppercase text-muted-foreground">
                      <th className="py-2 pr-3 text-left">유형</th>
                      <th className="py-2 pr-3 text-left">설명</th>
                      <th className="py-2 pr-3 text-right">수량</th>
                      <th className="py-2 pr-3 text-right">단가</th>
                      <th className="py-2 text-right">금액</th>
                    </tr>
                  </thead>
                  <tbody>
                    {invoice.items.map((item) => (
                      <tr key={item.id} className="border-b last:border-0">
                        <td className="py-2 pr-3">{ITEM_TYPE_LABEL[item.type] ?? item.type}</td>
                        <td className="py-2 pr-3">{item.description}</td>
                        <td className="py-2 pr-3 text-right">{item.quantity}</td>
                        <td className="py-2 pr-3 text-right">{formatMoney(item.unitPrice, invoice.currency)}</td>
                        <td className="py-2 text-right font-medium">{formatMoney(item.amount, invoice.currency)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </CardContent>
        </Card>

        {/* 금액 + 일정 */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">결제 정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 text-sm">
            <Row label="소계" value={formatMoney(invoice.subtotal, invoice.currency)} />
            <Row label="세금" value={formatMoney(invoice.taxAmount, invoice.currency)} />
            <Separator />
            <Row label="총액" value={formatMoney(invoice.totalAmount, invoice.currency)} bold />
            <Separator />
            <Row label="발행일"     value={invoice.issuedAt ? new Date(invoice.issuedAt).toLocaleDateString() : "-"} />
            <Row label="납부 기한"  value={invoice.dueDate ?? "-"} />
            <Row label="납부 완료일" value={invoice.paidAt ? new Date(invoice.paidAt).toLocaleString() : "-"} />
            {invoice.note && (
              <>
                <Separator />
                <div>
                  <p className="text-xs uppercase text-muted-foreground mb-1">메모</p>
                  <p className="text-sm">{invoice.note}</p>
                </div>
              </>
            )}
          </CardContent>
        </Card>
      </div>
    </>
  );
}

function Row({ label, value, bold = false }: { label: string; value: string; bold?: boolean }) {
  return (
    <div className="flex justify-between">
      <span className="text-muted-foreground">{label}</span>
      <span className={bold ? "font-semibold text-base" : ""}>{value}</span>
    </div>
  );
}
