"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useMemo, useState } from "react";
import { ArrowRight, Building2, KeyRound, Mail, UserRound } from "lucide-react";
import { authService } from "./auth.service";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Field, FieldDescription, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Toaster } from "@/components/ui/sonner";
import type { SignupRequest } from "./auth.types";

/**
 * 사용자 가입 폼 컴포넌트입니다.
 * 가입 성공 시 /login으로 이동합니다.
 */
export function SignupForm() {
  const router = useRouter();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [organizationName, setOrganizationName] = useState("");
  const [currency, setCurrency] = useState("KRW");
  const [password, setPassword] = useState("");
  const [passwordConfirm, setPasswordConfirm] = useState("");
  const [accepted, setAccepted] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const passwordHelp = useMemo(() => {
    if (!password) return "8자 이상, 영문과 숫자를 포함하세요.";
    if (password.length < 8) return "비밀번호는 8자 이상이어야 합니다.";
    if (!/[A-Za-z]/.test(password) || !/[0-9]/.test(password)) {
      return "영문과 숫자를 모두 포함해야 합니다.";
    }
    return "사용 가능한 비밀번호입니다.";
  }, [password]);

  function validate(): string {
    if (!name.trim()) return "이름을 입력하세요.";
    if (!organizationName.trim()) return "회사 또는 조직명을 입력하세요.";
    if (!email.includes("@")) return "올바른 이메일을 입력하세요.";
    if (password.length < 8 || !/[A-Za-z]/.test(password) || !/[0-9]/.test(password)) {
      return "비밀번호는 8자 이상이며 영문과 숫자를 포함해야 합니다.";
    }
    if (password !== passwordConfirm) return "비밀번호 확인이 일치하지 않습니다.";
    if (!accepted) return "서비스 이용 동의가 필요합니다.";
    return "";
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const validationError = validate();
    setErrorMessage(validationError);
    if (validationError) return;

    const payload: SignupRequest = {
      name: name.trim(),
      email: email.trim(),
      password,
      organizationName: organizationName.trim(),
      currency,
      accountType: "VIRTUAL",
    };

    setIsSubmitting(true);
    try {
      await authService.signup(payload);
      router.push(`/signup/complete?email=${encodeURIComponent(email.trim())}`);
    } catch {
      setErrorMessage("가입에 실패했습니다. 이미 사용 중인 이메일이거나 서버 오류입니다.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="signup-shell">
      <Toaster position="top-right" />

      <section className="signup-panel signup-panel-form">
        <Building2 />
        <p>Core Banking SaaS</p>
        <h1>서비스 가입</h1>
        <span>사용자 SaaS 워크스페이스 생성을 위한 기본 정보를 입력하세요.</span>

        {errorMessage && (
          <Alert variant="destructive">
            <AlertTitle>확인 필요</AlertTitle>
            <AlertDescription>{errorMessage}</AlertDescription>
          </Alert>
        )}

        <form className="signup-form" onSubmit={handleSubmit}>
          <FieldGroup>
            <Field>
              <FieldLabel htmlFor="signup-name">이름</FieldLabel>
              <div className="signup-input-wrap">
                <UserRound />
                <Input
                  id="signup-name"
                  autoComplete="name"
                  value={name}
                  onChange={(event) => setName(event.target.value)}
                  placeholder="홍길동"
                />
              </div>
            </Field>
            <Field>
              <FieldLabel htmlFor="signup-email">이메일</FieldLabel>
              <div className="signup-input-wrap">
                <Mail />
                <Input
                  id="signup-email"
                  type="email"
                  autoComplete="username"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  placeholder="user@example.com"
                />
              </div>
            </Field>
            <Field>
              <FieldLabel htmlFor="signup-org">회사 또는 조직명</FieldLabel>
              <Input
                id="signup-org"
                value={organizationName}
                onChange={(event) => setOrganizationName(event.target.value)}
                placeholder="Revy Capital"
              />
            </Field>
            <Field>
              <FieldLabel htmlFor="signup-currency">기본 통화</FieldLabel>
              <select
                id="signup-currency"
                className="bank-select signup-select"
                value={currency}
                onChange={(event) => setCurrency(event.target.value)}
              >
                <option value="KRW">KRW</option>
                <option value="USD">USD</option>
                <option value="EUR">EUR</option>
                <option value="JPY">JPY</option>
              </select>
            </Field>
            <Field>
              <FieldLabel htmlFor="signup-password">비밀번호</FieldLabel>
              <div className="signup-input-wrap">
                <KeyRound />
                <Input
                  id="signup-password"
                  type="password"
                  autoComplete="new-password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                />
              </div>
              <FieldDescription>{passwordHelp}</FieldDescription>
            </Field>
            <Field>
              <FieldLabel htmlFor="signup-password-confirm">비밀번호 확인</FieldLabel>
              <div className="signup-input-wrap">
                <KeyRound />
                <Input
                  id="signup-password-confirm"
                  type="password"
                  autoComplete="new-password"
                  value={passwordConfirm}
                  onChange={(event) => setPasswordConfirm(event.target.value)}
                />
              </div>
            </Field>
            <Field orientation="horizontal" className="signup-check-field">
              <Checkbox
                id="signup-terms"
                checked={accepted}
                onCheckedChange={(value) => setAccepted(value === true)}
              />
              <FieldLabel htmlFor="signup-terms">
                서비스 이용 및 개인정보 처리 안내에 동의합니다.
              </FieldLabel>
            </Field>
          </FieldGroup>

          <Button type="submit" size="lg" disabled={isSubmitting}>
            즉시 가입
            <ArrowRight data-icon="inline-end" />
          </Button>
        </form>

        <div>
          <Button variant="outline" asChild>
            <Link href="/">메인으로</Link>
          </Button>
          <Button variant="ghost" asChild>
            <Link href="/login">이미 계정이 있습니다</Link>
          </Button>
        </div>
      </section>
    </main>
  );
}
