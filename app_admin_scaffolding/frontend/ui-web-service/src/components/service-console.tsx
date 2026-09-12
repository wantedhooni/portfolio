"use client";

import { useEffect, useState } from "react";

import type { TokenResponse } from "@/lib/types";

type ServiceConsoleProps = {
  apiBaseUrl: string;
};

const STORAGE_KEY = "service-auth-result";

type StoredAuthResult = {
  email: string;
  tokens: TokenResponse;
};

export function ServiceConsole({ apiBaseUrl }: ServiceConsoleProps) {
  const [stored, setStored] = useState<StoredAuthResult | null>(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [pending, setPending] = useState(false);

  useEffect(() => {
    const raw = window.sessionStorage.getItem(STORAGE_KEY);

    if (!raw) {
      return;
    }

    try {
      setStored(JSON.parse(raw) as StoredAuthResult);
    } catch {
      window.sessionStorage.removeItem(STORAGE_KEY);
    }
  }, []);

  async function onReissue() {
    if (!stored?.tokens.refreshToken) {
      setError("로그인 응답이 없어 재발급을 실행할 수 없습니다.");
      return;
    }

    setPending(true);
    setMessage("");
    setError("");

    try {
      const response = await fetch("/api/reissue", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          refreshToken: stored.tokens.refreshToken,
        }),
      });

      const payload = (await response.json()) as {
        success?: boolean;
        data?: TokenResponse;
        error?: {
          message?: string;
        };
      };

      if (!response.ok || !payload.success || !payload.data) {
        throw new Error(payload.error?.message || "재발급 요청에 실패했습니다.");
      }

      const nextValue = {
        ...stored,
        tokens: payload.data,
      };

      window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify(nextValue));
      setStored(nextValue);
      setMessage("토큰 재발급이 완료되었습니다.");
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : "알 수 없는 오류가 발생했습니다.",
      );
    } finally {
      setPending(false);
    }
  }

  function onClear() {
    window.sessionStorage.removeItem(STORAGE_KEY);
    setStored(null);
    setMessage("");
    setError("");
  }

  return (
    <section className="panel console-panel">
      <div className="console-head">
        <span className="eyebrow">Live API Console</span>
        <h2>실제 Service API 응답 확인</h2>
        <p>
          현재 앱은 `signup`, `signin`, `reissue` 세 엔드포인트에 직접 맞춰져
          있습니다. 로그인 후 받은 토큰 응답을 여기서 확인하고 재발급도 테스트할 수
          있습니다.
        </p>
      </div>
      <div className="console-meta">
        <div>
          <span>API Base URL</span>
          <strong>{apiBaseUrl}</strong>
        </div>
        <div>
          <span>Stored session</span>
          <strong>{stored ? "available" : "empty"}</strong>
        </div>
      </div>
      {stored ? (
        <div className="console-result">
          <article>
            <span>Email</span>
            <strong>{stored.email}</strong>
          </article>
          <article>
            <span>Token Type</span>
            <strong>{stored.tokens.tokenType}</strong>
          </article>
          <article>
            <span>Access Token</span>
            <strong>{maskToken(stored.tokens.accessToken)}</strong>
          </article>
          <article>
            <span>Refresh Token</span>
            <strong>{maskToken(stored.tokens.refreshToken)}</strong>
          </article>
        </div>
      ) : (
        <div className="empty-console">
          아직 저장된 로그인 응답이 없습니다. 먼저 `Signin` 화면에서 실제 Service API
          로그인 요청을 수행하세요.
        </div>
      )}
      {message ? <div className="message success">{message}</div> : null}
      {error ? <div className="message error">{error}</div> : null}
      <div className="hero-actions">
        <button className="primary-button" disabled={pending} onClick={onReissue}>
          {pending ? "재발급 중..." : "토큰 재발급"}
        </button>
        <button className="secondary-button" onClick={onClear} type="button">
          저장된 응답 지우기
        </button>
      </div>
    </section>
  );
}

function maskToken(token: string) {
  if (token.length <= 18) {
    return token;
  }

  return `${token.slice(0, 12)}...${token.slice(-6)}`;
}
