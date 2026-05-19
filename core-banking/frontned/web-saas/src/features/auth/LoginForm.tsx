"use client";

import type { FormEvent } from "react";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { LockKeyhole, ShieldCheck } from "lucide-react";

import { DEMO_PASSWORD, DEMO_USER } from "@/constant/constants";
import { useSession } from "@/workspace/useSession";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Field, FieldDescription, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";

const DEFAULT_EMAIL = DEMO_USER || "demo@corebanking.local";
const DEFAULT_PASSWORD = DEMO_PASSWORD || "demo1234!";

/**
 * 로그인 폼 컴포넌트입니다.
 * 로그인 성공 시 /workspace로 이동합니다.
 */
export function LoginForm() {
  const router = useRouter();
  const { login, isLoading } = useSession();
  const [email, setEmail] = useState(DEFAULT_EMAIL);
  const [password, setPassword] = useState(DEFAULT_PASSWORD);
  const [error, setError] = useState("");

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError("");
    try {
      await login({ email, password });
      router.push("/workspace");
    } catch {
      setError("이메일 또는 비밀번호를 확인하세요.");
    }
  }

  return (
    <main className="bank-login-shell">
      <section className="bank-login-visual" aria-label="코어뱅킹 SaaS 소개">
        <div className="bank-login-grid" />
        <div className="bank-login-copy">
          <Badge variant="secondary">Core Banking SaaS</Badge>
          <h1>Revy Bank OS</h1>
          <p>
            고객 계좌, 현금 이동, 주식 거래, 포트폴리오 상태를 하나의 운영 화면에서
            확인하고 처리합니다.
          </p>
        </div>
        <div className="bank-orbit" aria-hidden="true">
          <span />
          <span />
          <span />
        </div>
      </section>

      <section className="bank-login-panel" aria-label="로그인">
        <div className="bank-login-heading">
          <ShieldCheck />
          <div>
            <h2>사용자 워크스페이스</h2>
            <p>백엔드 API 서버에 연결해 실제 데이터를 조회합니다.</p>
          </div>
        </div>

        <form className="bank-form" onSubmit={handleSubmit}>
          <FieldGroup>
            <Field>
              <FieldLabel htmlFor="email">이메일</FieldLabel>
              <Input
                id="email"
                type="email"
                autoComplete="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </Field>
            <Field>
              <FieldLabel htmlFor="password">비밀번호</FieldLabel>
              <Input
                id="password"
                type="password"
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              <FieldDescription>
                기본 계정: {DEFAULT_EMAIL} / {DEFAULT_PASSWORD}
              </FieldDescription>
            </Field>
          </FieldGroup>

          {error && (
            <Alert variant="destructive">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          <Button type="submit" disabled={isLoading} size="lg">
            <LockKeyhole data-icon="inline-start" />
            로그인
          </Button>
        </form>
      </section>
    </main>
  );
}
