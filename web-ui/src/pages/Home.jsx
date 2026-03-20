import React from 'react'
import { Link } from 'react-router-dom'

export default function Home() {
  return (
    <div className="intro-page">
      <section className="intro-hero">
        <div className="intro-hero__content intro-hero__content--accent">
          <span className="intro-eyebrow">Trading Workspace</span>
          <h2>시장 조회부터 계좌, 환전, 주문까지 하나의 흐름으로 연결한 증권 화면</h2>
          <p className="intro-lead">
            시장 정보 확인과 주문 준비에 필요한 화면을 한곳에 모았습니다.
            핵심 정보와 다음 액션이 자연스럽게 이어지도록 단순하게 정리했습니다.
          </p>

          <div className="intro-hero__cta">
            <Link to="/login" className="primary-button">로그인</Link>
            <Link to="/chart" className="ghost-button">시장 화면 보기</Link>
          </div>

          <div className="intro-hero__meta">
            <div className="intro-stat">
              <span className="intro-stat__label">접근 방식</span>
              <strong>로그인 후 전체 기능 사용</strong>
              <span className="intro-muted">계좌, 환전, 주문 화면으로 이어집니다.</span>
            </div>
            <div className="intro-stat">
              <span className="intro-stat__label">Core Flow</span>
              <strong>Market → Account → Exchange → Trade</strong>
              <span className="intro-muted">정보 확인과 실행 흐름을 한 방향으로 정리</span>
            </div>
            <div className="intro-stat">
              <span className="intro-stat__label">Architecture</span>
              <strong>Gradle 멀티모듈 + React UI</strong>
              <span className="intro-muted">단순한 구조 위에 업무 흐름 중심으로 화면 구성</span>
            </div>
          </div>
        </div>

        <div className="intro-hero__panel">
          <article className="intro-panel-card intro-panel-card--highlight">
            <span className="intro-panel-title">시작 순서</span>
            <div className="intro-steps">
              <div className="intro-step">
                <span className="intro-step__index">01</span>
                <span className="intro-step__label">로그인</span>
              </div>
              <div className="intro-step">
                <span className="intro-step__index">02</span>
                <span className="intro-step__label">계좌와 잔액 확인</span>
              </div>
              <div className="intro-step">
                <span className="intro-step__index">03</span>
                <span className="intro-step__label">환전 또는 주문 진행</span>
              </div>
            </div>
          </article>

          <article className="intro-panel-card">
            <span className="intro-panel-title">화면 구성</span>
            <p>
              홈에서는 전체 흐름을 안내하고, 각 업무 화면에서는 필요한 정보와 액션을 먼저 보이게 구성했습니다.
            </p>
          </article>

          <article className="intro-panel-card">
            <span className="intro-panel-title">테스트 계정</span>
            <p>
              `user1@example.com` 부터 `user10@example.com` 까지 사용할 수 있으며 비밀번호는 모두 `Password!` 입니다.
            </p>
          </article>
        </div>
      </section>

      <section className="intro-grid">
        <article className="intro-card">
          <h3>계좌 관리</h3>
          <p className="intro-muted">계좌 개설, 입금, 출금, 이체를 한 화면 흐름으로 확인합니다.</p>
          <ul>
            <li>통화와 상태 기준 필터링</li>
            <li>계좌별 잔액과 사용 가능 금액 확인</li>
            <li>즉시 액션 후 목록 재조회</li>
          </ul>
        </article>

        <article className="intro-card">
          <h3>환전 경험</h3>
          <p className="intro-muted">보유 계좌를 보며 환전 비율과 예상 결과를 함께 확인합니다.</p>
          <ul>
            <li>입력 중심 구조 대신 결과 중심 배치</li>
            <li>계좌 카드와 환전 카드 분리</li>
            <li>모바일에서도 한 열 흐름 유지</li>
          </ul>
        </article>

        <article className="intro-card">
          <h3>주문 및 시세</h3>
          <p className="intro-muted">종목 검색, 시세, 주문서, 주문내역을 가까운 문맥으로 묶었습니다.</p>
          <ul>
            <li>차트와 주문 진입 동선 최소화</li>
            <li>호가와 주문 패널의 시각적 분리</li>
            <li>데모 주문 상태 확인 가능</li>
          </ul>
        </article>
      </section>

      <section className="intro-footer-card">
        <div>
          <h3>바로 시작</h3>
          <p className="intro-muted">로그인 후 시장, 계좌, 환전, 주문 화면을 순서대로 사용할 수 있습니다.</p>
        </div>
        <div className="intro-footer__actions">
          <Link to="/login" className="primary-button">로그인</Link>
          <Link to="/signup" className="ghost-button">회원가입</Link>
        </div>
      </section>
    </div>
  )
}
