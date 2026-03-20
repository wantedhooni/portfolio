import React from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthProvider'

export default function Home() {
  const auth = useAuth()
  const isLoggedIn = Boolean(auth?.user)
  const primaryCta = isLoggedIn ? { to: '/chart', label: '시장 바로 보기' } : { to: '/login', label: '로그인' }
  const secondaryCta = isLoggedIn ? { to: '/account', label: '내 계좌 확인' } : { to: '/signup', label: '회원가입' }
  const actionCards = [
    { title: '시장 확인', text: '관심 종목 시세와 차트를 보고 바로 주문으로 이동합니다.', to: '/chart', action: '시장 열기' },
    { title: '계좌 관리', text: '보유 계좌, 잔액, 입출금과 이체를 한 화면에서 처리합니다.', to: '/account', action: '계좌 보기' },
    { title: '환전 계산', text: '기준 통화와 대상 통화를 선택해 환전 금액을 빠르게 확인합니다.', to: '/exchange', action: '환전 보기' },
    { title: '주문 확인', text: '최근 주문 상태와 취소 가능 주문을 바로 확인합니다.', to: '/orders', action: '주문 보기' },
  ]

  return (
    <div className="intro-page">
      <section className="portal-hero">
        <div className="portal-hero__main">
          <span className="intro-eyebrow">Customer Front</span>
          <h1 className="portal-hero__title">필요한 금융 작업을 바로 시작할 수 있는 고객 화면</h1>
          <p className="portal-hero__text">
            메뉴 설명보다 다음 행동이 먼저 보이도록 다시 정리했습니다. 시세 확인, 계좌 관리, 환전 계산, 주문 확인까지 한 흐름으로 이동할 수 있습니다.
          </p>
          <div className="portal-hero__actions">
            <Link to={primaryCta.to} className="primary-button">{primaryCta.label}</Link>
            <Link to={secondaryCta.to} className="ghost-button">{secondaryCta.label}</Link>
          </div>
        </div>
        <div className="portal-hero__side">
          <div className="portal-quick-card">
            <span className="portal-quick-card__label">빠른 안내</span>
            <strong>{isLoggedIn ? '로그인 상태입니다.' : '로그인하면 전체 기능을 사용할 수 있습니다.'}</strong>
            <p>{isLoggedIn ? '시장과 계좌 화면으로 바로 이동해 작업을 시작하세요.' : '테스트 계정으로 흐름을 바로 확인할 수 있습니다.'}</p>
          </div>
          <div className="portal-quick-card">
            <span className="portal-quick-card__label">테스트 계정</span>
            <strong>user1@example.com</strong>
            <p>Password!</p>
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
            <Link to={card.to} className="portal-card__link">{card.action}</Link>
          </article>
        ))}
      </section>

      <section className="portal-flow">
        <div className="portal-flow__header">
          <h2>권장 사용 흐름</h2>
          <p>처음 들어온 사용자가 헤매지 않도록 가장 많이 쓰는 순서대로 정리했습니다.</p>
        </div>
        <div className="portal-flow__steps">
          <div className="portal-step">
            <span>1</span>
            <strong>시장 화면에서 종목을 확인합니다.</strong>
          </div>
          <div className="portal-step">
            <span>2</span>
            <strong>계좌에서 주문 가능 금액과 통화를 확인합니다.</strong>
          </div>
          <div className="portal-step">
            <span>3</span>
            <strong>필요하면 환전 계산 후 거래 화면에서 주문합니다.</strong>
          </div>
          <div className="portal-step">
            <span>4</span>
            <strong>주문 화면에서 접수와 체결 상태를 확인합니다.</strong>
          </div>
        </div>
      </section>
    </div>
  )
}
