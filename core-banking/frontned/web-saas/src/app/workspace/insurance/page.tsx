"use client";

import { useCallback, useEffect, useState } from "react";
import { HeartHandshake, PackageOpen, ShieldCheck, FileText } from "lucide-react";
import { toast } from "sonner";

import { useWorkspaceContext } from "@/workspace/WorkspaceProvider";
import { PageHeader } from "@/workspace/PageHeader";
import { insuranceService } from "@/features/insurance/insurance.service";
import type {
  InsuranceClaim,
  InsurancePolicy,
  InsuranceProduct,
} from "@/features/insurance/insurance.types";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ProductCatalog } from "@/features/insurance/ProductCatalog";
import { MyPolicies } from "@/features/insurance/MyPolicies";
import { MyClaims } from "@/features/insurance/MyClaims";
import { EnrollDialog } from "@/features/insurance/EnrollDialog";
import { SubmitClaimDialog } from "@/features/insurance/SubmitClaimDialog";

/**
 * 보험 페이지입니다.
 * 상품 카탈로그, 내 증권, 내 청구 3개 탭으로 구성됩니다.
 */
export default function InsurancePage() {
  const { accounts, user } = useWorkspaceContext();

  const [tab, setTab] = useState<"catalog" | "policies" | "claims">("catalog");
  const [products, setProducts] = useState<InsuranceProduct[]>([]);
  const [policies, setPolicies] = useState<InsurancePolicy[]>([]);
  const [claims, setClaims]     = useState<InsuranceClaim[]>([]);
  const [loading, setLoading]   = useState(false);

  const [enrollProduct, setEnrollProduct] = useState<InsuranceProduct | null>(null);
  const [claimPolicy, setClaimPolicy]     = useState<InsurancePolicy | null>(null);

  const loadAll = useCallback(async () => {
    setLoading(true);
    try {
      const [prod, pol, clm] = await Promise.all([
        insuranceService.searchProducts({}, { page: 0, size: 30 }),
        insuranceService.myPolicies({ page: 0, size: 50 }),
        insuranceService.myClaims({ page: 0, size: 50 }),
      ]);
      setProducts(prod.content);
      setPolicies(pol.content);
      setClaims(clm.content);
    } catch {
      toast.error("보험 데이터 조회 실패");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { void loadAll(); }, [loadAll]);

  return (
    <>
      <PageHeader title="보험" />

      <Tabs value={tab} onValueChange={(v) => setTab(v as typeof tab)} className="w-full">
        <TabsList className="w-full sm:w-auto">
          <TabsTrigger value="catalog">
            <PackageOpen className="size-4" data-icon="inline-start" />
            상품 카탈로그
          </TabsTrigger>
          <TabsTrigger value="policies">
            <ShieldCheck className="size-4" data-icon="inline-start" />
            내 증권 ({policies.length})
          </TabsTrigger>
          <TabsTrigger value="claims">
            <FileText className="size-4" data-icon="inline-start" />
            내 청구 ({claims.length})
          </TabsTrigger>
        </TabsList>

        <TabsContent value="catalog" className="mt-4">
          <ProductCatalog
            products={products}
            loading={loading}
            onEnroll={setEnrollProduct}
          />
        </TabsContent>

        <TabsContent value="policies" className="mt-4">
          <MyPolicies
            policies={policies}
            products={products}
            loading={loading}
            onSubmitClaim={setClaimPolicy}
          />
        </TabsContent>

        <TabsContent value="claims" className="mt-4">
          <MyClaims claims={claims} loading={loading} />
        </TabsContent>
      </Tabs>

      <EnrollDialog
        product={enrollProduct}
        accounts={accounts}
        currentUserId={user?.id ?? null}
        open={!!enrollProduct}
        onClose={() => setEnrollProduct(null)}
        onSuccess={() => {
          setEnrollProduct(null);
          void loadAll();
          toast.success("보험 가입이 완료되었습니다.");
          setTab("policies");
        }}
      />

      <SubmitClaimDialog
        policy={claimPolicy}
        accounts={accounts}
        open={!!claimPolicy}
        onClose={() => setClaimPolicy(null)}
        onSuccess={() => {
          setClaimPolicy(null);
          void loadAll();
          toast.success("청구가 접수되었습니다.");
          setTab("claims");
        }}
      />
    </>
  );
}
