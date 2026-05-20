import Link from "next/link";
import {
  ArrowRight,
  Building2,
  CheckCircle2,
  Gauge,
  Landmark,
  type LucideIcon,
  Layers3,
  LockKeyhole,
} from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";

export default function Home() {
  return (
    <main className="landing-shell">
      <header className="landing-header" aria-label="상단 메뉴">
        <Link href="/" className="landing-brand" aria-label="Revy Bank OS 홈">
          <span>RB</span>
          <strong>Revy Bank OS</strong>
        </Link>
        <nav className="landing-nav" aria-label="랜딩 내비게이션">
          <a href="#platform">플랫폼</a>
          <a href="#operations">운영</a>
          <a href="#security">보안</a>
        </nav>
        <div className="landing-actions">
          <Button variant="ghost" asChild>
            <Link href="/login">
              <LockKeyhole data-icon="inline-start" />
              로그인
            </Link>
          </Button>
          <Button asChild>
            <Link href="/signup">
              가입
              <ArrowRight data-icon="inline-end" />
            </Link>
          </Button>
        </div>
      </header>

      <section className="landing-hero" aria-label="코어뱅킹 SaaS 소개">
        <div className="landing-hero-copy">
          <Badge variant="secondary">Core Banking SaaS</Badge>
          <h1>Revy Bank OS</h1>
          <p>
            계좌 원장, 현금 이동, 투자 거래, 포트폴리오 상태를 하나의 사용자 SaaS
            환경에서 운영합니다.
          </p>
          <div className="landing-hero-actions">
            <Button size="lg" asChild>
              <Link href="/signup">
                서비스 가입
                <ArrowRight data-icon="inline-end" />
              </Link>
            </Button>
            <Button size="lg" variant="outline" asChild>
              <Link href="/login">데모 로그인</Link>
            </Button>
          </div>
        </div>

        <div className="landing-visual" aria-hidden="true">
          <div className="landing-ledger">
            <div className="landing-ledger-top">
              <span />
              <span />
              <span />
            </div>
            <div className="landing-ledger-total">
              <small>Total assets</small>
              <strong>₩228.4B</strong>
              <em>+7.21%</em>
            </div>
            <div className="landing-ledger-chart">
              <i />
              <i />
              <i />
              <i />
              <i />
              <i />
            </div>
            <div className="landing-ledger-list">
              <span>
                <b>Deposit</b>
                <em>Completed</em>
              </span>
              <span>
                <b>Buy 005930</b>
                <em>Matched</em>
              </span>
              <span>
                <b>Dividend</b>
                <em>Posted</em>
              </span>
            </div>
          </div>
        </div>
      </section>

      <section id="platform" className="landing-band" aria-label="플랫폼 핵심 기능">
        <div>
          <span>Platform</span>
          <h2>사용자 계좌와 투자 원장을 같은 흐름으로 처리합니다.</h2>
        </div>
        <div className="landing-feature-row">
          <Feature icon={Landmark} title="계좌 원장" text="계좌 생성, 잔고, 상태, 입출금 흐름을 API 계약에 맞춰 연결합니다." />
          <Feature icon={Gauge} title="포트폴리오" text="현금, 평가금액, 손익률, 보유종목을 운영자가 바로 스캔합니다." />
          <Feature icon={Layers3} title="거래 처리" text="매수, 매도, 배당, 세금, 수수료를 한 화면에서 요청합니다." />
        </div>
      </section>

      <section id="operations" className="landing-proof" aria-label="운영 흐름">
        <div>
          <Badge variant="outline">Operations</Badge>
          <h2>API 서버가 없어도 데모 모드로 전체 사용자 여정을 검증할 수 있습니다.</h2>
        </div>
        <ol>
          <li>
            <CheckCircle2 />
            <span>데모 또는 실제 API 로그인</span>
          </li>
          <li>
            <CheckCircle2 />
            <span>계좌 선택 후 입출금 요청</span>
          </li>
          <li>
            <CheckCircle2 />
            <span>주식 거래와 포트폴리오 확인</span>
          </li>
        </ol>
      </section>

      <section id="security" className="landing-final" aria-label="시작하기">
        <Building2 />
        <h2>코어뱅킹 SaaS 워크스페이스를 시작하세요.</h2>
        <p>기본 데모 계정으로 즉시 확인하거나, 백엔드 SAAS-API에 연결해 실제 흐름을 검증합니다.</p>
        <div>
          <Button asChild>
            <Link href="/login">로그인</Link>
          </Button>
          <Button variant="outline" asChild>
            <Link href="/signup">가입</Link>
          </Button>
        </div>
      </section>
    </main>
  );
}

function Feature({
  icon: Icon,
  title,
  text,
}: {
  icon: LucideIcon;
  title: string;
  text: string;
}) {
  return (
    <article>
      <Icon />
      <h3>{title}</h3>
      <p>{text}</p>
    </article>
  );
}
