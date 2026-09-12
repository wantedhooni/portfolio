"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

type Mode = "signup" | "signin";

const CONTENT = {
  signup: {
    title: "가입하고 바로 시작하세요",
    description: "운영 서비스 MVP처럼, 꼭 필요한 정보만 받아 가입을 끝냅니다.",
    submitLabel: "Signup",
    endpoint: "/api/signup",
    nextPath: "/signin",
  },
  signin: {
    title: "다시 로그인하세요",
    description: "자주 방문하는 고객이 빠르게 들어갈 수 있는 기본 로그인 화면입니다.",
    submitLabel: "Signin",
    endpoint: "/api/signin",
    nextPath: "/",
  },
} as const;

export function AuthForm({ mode }: { mode: Mode }) {
  const router = useRouter();
  const copy = CONTENT[mode];
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [nickName, setNickName] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [tokenPreview, setTokenPreview] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function onSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setMessage("");
    setError("");
    setTokenPreview("");

    try {
      const normalizedEmail = email.trim().toLowerCase();
      const normalizedNickName = nickName.trim();
      if (!normalizedEmail || !password) {
        throw new Error("이메일과 비밀번호를 입력해 주세요.");
      }
      if (mode === "signup" && !normalizedNickName) {
        throw new Error("닉네임을 입력해 주세요.");
      }

      const response = await fetch(copy.endpoint, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(
          mode === "signup"
            ? { email: normalizedEmail, password, nickName: normalizedNickName }
            : { email: normalizedEmail, password },
        ),
      });

      const payload = (await response.json()) as {
        success?: boolean;
        data?: {
          message?: string;
          tokenType?: string;
          accessToken?: string;
          refreshToken?: string;
        };
        error?: {
          message?: string;
        };
      };

      if (!response.ok || !payload.success) {
        throw new Error(payload.error?.message || "요청에 실패했습니다.");
      }

      if (mode === "signup") {
        setMessage(payload.data?.message || "회원가입이 완료되었습니다.");
      } else {
        window.sessionStorage.setItem(
          "service-auth-result",
          JSON.stringify({
            email,
            tokens: payload.data,
          }),
        );
        setMessage("로그인이 완료되었습니다.");
        setTokenPreview(
          `${payload.data?.tokenType || "Bearer"} ${String(
            payload.data?.accessToken || "",
          ).slice(0, 16)}...`,
        );
      }

      router.refresh();
      router.push(copy.nextPath);
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : "알 수 없는 오류가 발생했습니다.",
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="panel auth-card">
      <div className="auth-card-head">
        <span className="eyebrow">{mode.toUpperCase()}</span>
        <h1>{copy.title}</h1>
        <p>{copy.description}</p>
      </div>
      <form className="auth-form" onSubmit={onSubmit}>
        <label>
          Email
          <input
            autoComplete="email"
            onChange={(event) => setEmail(event.target.value)}
            placeholder="name@example.com"
            required
            type="email"
            value={email}
          />
        </label>
        {mode === "signup" ? (
          <label>
            Nickname
            <input
              autoComplete="nickname"
              maxLength={20}
              onChange={(event) => setNickName(event.target.value)}
              placeholder="nickname"
              required
              type="text"
              value={nickName}
            />
          </label>
        ) : null}
        <label>
          Password
          <input
            autoComplete={mode === "signin" ? "current-password" : "new-password"}
            onChange={(event) => setPassword(event.target.value)}
            placeholder="password"
            minLength={mode === "signup" ? 8 : undefined}
            required
            type="password"
            value={password}
          />
        </label>
        {message ? <div className="message success">{message}</div> : null}
        {error ? <div className="message error">{error}</div> : null}
        {tokenPreview ? (
          <div className="token-preview">Access token preview: {tokenPreview}</div>
        ) : null}
        <button className="primary-button" disabled={submitting} type="submit">
          {submitting ? "Loading..." : copy.submitLabel}
        </button>
      </form>
      <div className="auth-card-foot">
        <span>Email-based access</span>
        <span>MVP customer flow</span>
      </div>
    </section>
  );
}
