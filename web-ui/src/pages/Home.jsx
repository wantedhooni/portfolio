import React from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthProvider'

export default function Home() {
  const auth = useAuth()
  const isLoggedIn = Boolean(auth?.user)
  const primaryCta = isLoggedIn ? { to: '/chart', label: '시장 보기' } : { to: '/login', label: '로그인' }
  const secondaryCta = isLoggedIn ? { to: '/account', label: '계좌 보기' } : { to: '/signup', label: '회원가입' }
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
          <span className="intro-eyebrow">고객 화면</span>
          <h1 className="portal-hero__title">자주 쓰는 메뉴만 간단하게 모았습니다</h1>
          <p className="portal-hero__text">
            시세 확인, 계좌 조회, 환전 계산, 주문 확인을 한 화면에서 바로 이동할 수 있습니다.
          </p>
          <div className="portal-hero__actions">
            <Link to={primaryCta.to} className="primary-button">{primaryCta.label}</Link>
            <Link to={secondaryCta.to} className="ghost-button">{secondaryCta.label}</Link>
          </div>
        </div>
        <div className="portal-hero__side">
          <div className="portal-quick-card">
            <span className="portal-quick-card__label">{isLoggedIn ? '현재 상태' : '테스트 계정'}</span>
            <strong>{isLoggedIn ? '바로 이용할 수 있습니다' : '로그인 후 전체 기능 이용 가능'}</strong>
            <p>{isLoggedIn ? '시장, 계좌, 환전, 주문 메뉴를 바로 이용하세요.' : 'user1@example.com / Password!'}</p>
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
