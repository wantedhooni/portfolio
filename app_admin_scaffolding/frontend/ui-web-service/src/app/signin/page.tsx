import Link from "next/link";

import { AuthForm } from "@/components/auth-form";

export default function SigninPage() {
  return (
    <main className="auth-page">
      <section className="panel auth-story auth-story--signin">
        <span className="eyebrow">Member Access</span>
        <h2>돌아온 고객은 한 번에 들어가야 합니다</h2>
        <p>
          Airbnb나 Uber처럼 로그인은 망설이게 만들면 안 됩니다. 지금 버전은
          이메일 기반 진입에 집중하고, 성공 후 홈으로 바로 돌아가도록 구성했습니다.
        </p>
        <div className="story-grid">
          <article>
            <span>응답 구조</span>
            <strong>tokenType / access / refresh</strong>
          </article>
          <article>
            <span>로그인 후</span>
            <strong>홈으로 즉시 이동</strong>
          </article>
        </div>
        <Link className="secondary-button" href="/signup">
          처음이라면 회원가입
        </Link>
      </section>
      <AuthForm mode="signin" />
    </main>
  );
}
