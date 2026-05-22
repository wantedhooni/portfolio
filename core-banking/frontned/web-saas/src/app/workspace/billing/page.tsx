"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { FileText, RefreshCw, Receipt } from "lucide-react";
import { toast } from "sonner";

import { useWorkspaceContext } from "@/workspace/WorkspaceProvider";
import { PageHeader } from "@/workspace/PageHeader";
import { billingService } from "@/features/billing/billing.service";
import type { Invoice, InvoiceStatus } from "@/features/billing/billing.types";
import { formatMoney } from "@/lib/format";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

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

/**
 * 사용자 청구서 목록 페이지.
 * 본인 소유 계좌의 청구서만 표시. 계좌·상태 필터 지원.
 */
export default function BillingPage() {
  const { accounts } = useWorkspaceContext();
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [loading, setLoading] = useState(false);
  const [accountFilter, setAccountFilter] = useState<string>("all");
  const [statusFilter, setStatusFilter] = useState<string>("all");

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const page = await billingService.myInvoices({
        accountId: accountFilter === "all" ? undefined : Number(accountFilter),
        status:    statusFilter  === "all" ? undefined : (statusFilter as InvoiceStatus),
      });
      setInvoices(page.content);
    } catch {
      toast.error("청구서 조회 실패");
    } finally {
      setLoading(false);
    }
  }, [accountFilter, statusFilter]);

  useEffect(() => { void load(); }, [load]);

  const totalUnpaid = invoices
    .filter((i) => i.status === "ISSUED" || i.status === "OVERDUE")
    .reduce((sum, i) => sum + i.totalAmount, 0);

  return (
    <>
      <PageHeader title="청구서" />

      {/* 요약 */}
      <div className="grid gap-3 sm:grid-cols-3 mb-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-xs uppercase text-muted-foreground">전체</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-semibold">{invoices.length} <span className="text-sm font-normal text-muted-foreground">건</span></p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-xs uppercase text-muted-foreground">미납 합계</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-semibold">{formatMoney(totalUnpaid, "KRW")}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-xs uppercase text-muted-foreground">납부 완료</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-semibold">
              {invoices.filter((i) => i.status === "PAID").length} <span className="text-sm font-normal text-muted-foreground">건</span>
            </p>
          </CardContent>
        </Card>
      </div>

      {/* 필터 */}
      <div className="flex flex-wrap items-center gap-2 mb-4">
        <Select value={accountFilter} onValueChange={setAccountFilter}>
          <SelectTrigger className="w-[240px]">
            <SelectValue placeholder="계좌" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">전체 계좌</SelectItem>
            {accounts.map((a) => (
              <SelectItem key={a.id} value={String(a.id)}>
                {a.accountName} ({a.accountNumber})
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-[160px]">
            <SelectValue placeholder="상태" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">전체 상태</SelectItem>
            <SelectItem value="ISSUED">발행됨</SelectItem>
            <SelectItem value="OVERDUE">기한초과</SelectItem>
            <SelectItem value="PAID">납부완료</SelectItem>
            <SelectItem value="CANCELLED">취소됨</SelectItem>
          </SelectContent>
        </Select>

        <Button variant="outline" size="sm" onClick={load} disabled={loading}>
          <RefreshCw data-icon="inline-start" className={loading ? "animate-spin" : ""} />
          새로고침
        </Button>
      </div>

      {/* 리스트 */}
      {invoices.length === 0 ? (
        <Card>
          <CardContent className="py-12 text-center text-sm text-muted-foreground">
            <Receipt className="mx-auto mb-2 size-8 opacity-50" />
            표시할 청구서가 없습니다.
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-3">
          {invoices.map((inv) => {
            const account = accounts.find((a) => a.id === inv.accountId);
            return (
              <Link key={inv.id} href={`/workspace/billing/${inv.id}`} className="block">
                <Card className="hover:bg-accent/50 transition">
                  <CardContent className="py-4 flex items-center gap-4">
                    <FileText className="size-5 text-muted-foreground shrink-0" />
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="font-medium">{inv.billingPeriod}</span>
                        <Badge variant={STATUS_VARIANT[inv.status]}>{STATUS_LABEL[inv.status]}</Badge>
                      </div>
                      <p className="text-xs text-muted-foreground mt-0.5">
                        {account ? `${account.accountName} (${account.accountNumber})` : `계좌 #${inv.accountId}`}
                        {inv.dueDate && ` · 납부기한 ${inv.dueDate}`}
                      </p>
                    </div>
                    <div className="text-right shrink-0">
                      <p className="text-lg font-semibold">{formatMoney(inv.totalAmount, inv.currency)}</p>
                      <p className="text-xs text-muted-foreground">{inv.items.length} 항목</p>
                    </div>
                  </CardContent>
                </Card>
              </Link>
            );
          })}
        </div>
      )}
    </>
  );
}
