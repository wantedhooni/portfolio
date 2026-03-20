import React from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthProvider'

export default function Home() {
  const auth = useAuth()
  const isLoggedIn = Boolean(auth?.user)
  const primaryCta = isLoggedIn ? { to: '/chart', label: '시장 바로 보기' } : { to: '/login', label: '로그인' }
  const secondaryCta = isLoggedIn ? { to: '/account', label: '내 계좌 확인' } : { to: '/signup', label: '회원가입' }
  const actionCards = [
    { title: '시장', text: '종목과 차트 확인', to: '/chart' },
    { title: '계좌', text: '잔액과 입출금 관리', to: '/account' },
    { title: '환전', text: '환전 금액 계산', to: '/exchange' },
    { title: '주문', text: '주문 상태 확인', to: '/orders' },
  ]

  return (
    <div className="intro-page">
      <section className="portal-hero">
        <div className="portal-hero__main">
          <span className="intro-eyebrow">Customer Front</span>
          <h1 className="portal-hero__title">필요한 작업만 바로 시작할 수 있는 화면</h1>
          <p className="portal-hero__text">
            시세 확인, 계좌 관리, 환전 계산, 주문 확인만 단순하게 이동할 수 있도록 정리했습니다.
          </p>
          <div className="portal-hero__actions">
            <Link to={primaryCta.to} className="primary-button">{primaryCta.label}</Link>
            <Link to={secondaryCta.to} className="ghost-button">{secondaryCta.label}</Link>
          </div>
        </div>
        <div className="portal-hero__side">
          <div className="portal-quick-card">
            <span className="portal-quick-card__label">안내</span>
            <strong>{isLoggedIn ? '로그인 상태' : '테스트 계정 제공'}</strong>
            <p>{isLoggedIn ? '시장 또는 계좌 화면으로 바로 이동하세요.' : 'user1@example.com / Password!'}</p>
          </div>
        </div>
      </section>

      <section className="portal-grid">
        {actionCards.map(card => (
          <article key={card.title} className="portal-card">
            <div>
              <h3>{card.title}</h3>
              <p>{card.text}</p>
            </div>
            <Link to={card.to} className="portal-card__link">바로 가기</Link>
          </article>
        ))}
      </section>
    </div>
  )
}
