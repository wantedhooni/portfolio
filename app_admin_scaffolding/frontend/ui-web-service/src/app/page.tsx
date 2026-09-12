import Link from "next/link";

import { ServiceConsole } from "@/components/service-console";
import { getServiceApiStatus } from "@/lib/service-api";

export default async function HomePage() {
  const status = await getServiceApiStatus();

  return (
    <main className="home-page">
      <section className="hero panel">
        <div className="hero-copy">
          <span className="eyebrow">Service API MVP</span>
          <h1>현재 열려 있는 Service API 인증 기능을 바로 테스트하는 고객 앱</h1>
          <p>
            이 프론트는 가상의 비즈니스 화면이 아니라, 현재 Service API에 실제로
            존재하는 `signup`, `login`, `reissue` 엔드포인트에 맞춰 동작합니다.
            고객용 접근 화면으로 시작하되, 화면에서 바로 응답 구조까지 확인할 수 있게
            구성했습니다.
          </p>
          <div className="hero-actions">
            <Link className="primary-button" href="/signup">
              회원가입 테스트
            </Link>
            <Link className="secondary-button" href="/signin">
              로그인 테스트
            </Link>
          </div>
          <div className="hero-metrics">
            <div>
              <span>OpenAPI title</span>
              <strong>{status.title}</strong>
            </div>
            <div>
              <span>Version</span>
              <strong>{status.version}</strong>
            </div>
            <div>
              <span>Connection</span>
              <strong>{status.isReachable ? "connected" : "unavailable"}</strong>
            </div>
          </div>
        </div>
        <aside className="hero-spotlight">
          <div className="spotlight-badge">Live backend status</div>
          <h2>백엔드 스펙 기준으로만 화면을 구성했습니다</h2>
          <p>
            현재 2026-03-14 기준 로컬 Service API는{" "}
            {status.isReachable ? "연결 가능" : "연결 불가"} 상태로 보입니다. 연결이
            안 되면 화면은 유지되지만, 회원가입/로그인 요청은 실패 메시지를 그대로
            보여줍니다.
          </p>
          <div className="spotlight-stack">
            {status.endpoints.map((endpoint) => (
              <article key={endpoint.path}>
                <span>{endpoint.method}</span>
                <strong>{endpoint.path}</strong>
              </article>
            ))}
          </div>
        </aside>
      </section>

      <section className="feature-grid">
        <article className="feature-card feature-card--warm">
          <span>01. Signup</span>
          <h2>회원 가입 요청</h2>
          <p>Service API의 `/api/auth/signup`에 이메일과 비밀번호를 전달합니다.</p>
        </article>
        <article className="feature-card feature-card--cool">
          <span>02. Signin</span>
          <h2>로그인 후 토큰 응답 수신</h2>
          <p>로그인 성공 시 `tokenType`, `accessToken`, `refreshToken`을 저장합니다.</p>
        </article>
        <article className="feature-card feature-card--dark">
          <span>03. Reissue</span>
          <h2>저장된 refresh token으로 재발급</h2>
          <p>홈 화면에서 실제 `/api/auth/reissue` 요청까지 바로 테스트할 수 있습니다.</p>
        </article>
      </section>

      <section className="signal-band">
        <div>
          <span>Base URL</span>
          <strong>{status.baseUrl}</strong>
        </div>
        <div>
          <span>Checked At</span>
          <strong>{new Date(status.checkedAt).toLocaleString("ko-KR")}</strong>
        </div>
        <div>
          <span>Exposed API</span>
          <strong>{status.endpoints.length} auth endpoints</strong>
        </div>
      </section>

      <ServiceConsole apiBaseUrl={status.baseUrl} />

      <section className="mvp-showcase">
        <article className="showcase-card">
          <span className="eyebrow">Current Scope</span>
          <h2>현재 공개 범위는 인증 API 3개입니다</h2>
          <p>
            그래서 홈도 인증 테스트와 응답 확인에 맞춰 구성했습니다. 프로필 조회,
            예약 목록, 주문, 알림 같은 화면은 Service API가 열리면 그때 추가하는 게
            맞습니다.
          </p>
        </article>
        <div className="showcase-list">
          <article>
            <strong>회원가입</strong>
            <p>`SignupPayload.Req`와 `SignupPayload.Res` 구조를 그대로 사용합니다.</p>
          </article>
          <article>
            <strong>로그인</strong>
            <p>`LoginPayload.Res`의 access/refresh token 응답을 화면에서 확인합니다.</p>
          </article>
          <article>
            <strong>토큰 재발급</strong>
            <p>`TokenReissuePayload.Req`로 저장된 refresh token을 재사용합니다.</p>
          </article>
        </div>
      </section>
    </main>
  );
}
