import React from 'react'
import { Link } from 'react-router-dom'

export default function Home() {
  return (
    <div className="intro-page">
      <section className="intro-hero intro-hero--simple">
        <div className="intro-hero__content intro-hero__content--accent">
          <span className="intro-eyebrow">Trading Workspace</span>
          <h2>시장, 계좌, 환전, 주문 기능을 한 흐름으로 사용할 수 있는 화면</h2>
          <p className="intro-lead">
            필요한 기능만 빠르게 접근할 수 있도록 구성했습니다.
          </p>

          <div className="intro-hero__cta">
            <Link to="/login" className="primary-button">로그인</Link>
            <Link to="/signup" className="ghost-button">회원가입</Link>
          </div>
        </div>
      </section>

      <section className="intro-grid intro-grid--simple">
        <article className="intro-card">
          <h3>시장</h3>
          <p className="intro-muted">종목 조회와 차트 확인</p>
        </article>
        <article className="intro-card">
          <h3>계좌</h3>
          <p className="intro-muted">잔액 확인과 입출금, 이체</p>
        </article>
        <article className="intro-card">
          <h3>환전</h3>
          <p className="intro-muted">보유 통화 기준 환전 계산</p>
        </article>
        <article className="intro-card">
          <h3>주문</h3>
          <p className="intro-muted">시세 확인 후 주문 접수</p>
        </article>
      </section>

      <section className="intro-footer-card intro-footer-card--simple">
        <div>
          <h3>테스트 계정</h3>
          <p className="intro-muted">`user1@example.com` / `Password!`</p>
        </div>
        <div className="intro-footer__actions">
          <Link to="/chart" className="ghost-button">시장 화면 보기</Link>
        </div>
      </section>
    </div>
  )
}
