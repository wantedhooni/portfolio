import Link from "next/link";

import { AuthForm } from "@/components/auth-form";

export default function SignupPage() {
  return (
    <main className="auth-page">
      <section className="panel auth-story auth-story--signup">
        <span className="eyebrow">Create Account</span>
        <h2>처음 방문한 고객도 설명 없이 바로 가입할 수 있어야 합니다</h2>
        <p>
          Spotify나 Duolingo처럼 초기 가입은 짧고 분명해야 합니다. 고객은 계정을
          만들고, 바로 로그인해 서비스 안으로 들어갈 수 있어야 합니다.
        </p>
        <div className="story-grid">
          <article>
            <span>입력</span>
            <strong>이메일 / 비밀번호</strong>
          </article>
          <article>
            <span>완료 후</span>
            <strong>즉시 로그인으로 이동</strong>
          </article>
        </div>
        <Link className="secondary-button" href="/signin">
          이미 계정이 있어요
        </Link>
      </section>
      <AuthForm mode="signup" />
    </main>
  );
}
