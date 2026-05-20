"use client";

import { Suspense } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { CheckCircle2 } from "lucide-react";

import { Button } from "@/components/ui/button";

function CompleteContent() {
  const params = useSearchParams();
  const email = params.get("email") ?? "";

  return (
    <main className="signup-shell">
      <section className="signup-panel signup-complete">
        <CheckCircle2 />
        <h1>가입이 완료되었습니다</h1>
        <p>Core Banking SaaS 워크스페이스에 오신 것을 환영합니다.</p>

        {email && (
          <dl className="signup-receipt">
            <div>
              <dt>이메일</dt>
              <dd>{email}</dd>
            </div>
            <div>
              <dt>상태</dt>
              <dd>가입 완료 · 로그인 후 이용 가능</dd>
            </div>
          </dl>
        )}

        <div>
          <Button size="lg" asChild>
            <Link href="/login">로그인하기</Link>
          </Button>
          <Button variant="outline" asChild>
            <Link href="/">메인으로</Link>
          </Button>
        </div>
      </section>
    </main>
  );
}

export default function SignupCompletePage() {
  return (
    <Suspense>
      <CompleteContent />
    </Suspense>
  );
}
