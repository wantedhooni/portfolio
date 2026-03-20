import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthProvider'

export default function Login() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const auth = useAuth()

  const onSubmit = async e => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      await auth.login({ email, password })
      navigate('/')
    } catch (e) {
      setError(e.response?.data || 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-shell">
      <section className="auth-panel auth-panel--feature">
        <span className="intro-eyebrow">Sign In</span>
        <h2>거래 화면에 로그인</h2>
        <p className="auth-lead">
          시장 조회, 계좌, 환전, 주문 기능을 사용하려면 로그인하세요.
        </p>

        <div className="auth-demo-card">
          <strong>테스트 계정</strong>
          <span>user1@example.com</span>
          <span>Password!</span>
        </div>

        <div className="auth-checklist">
          <div className="auth-checklist__item">시장 시세와 차트 확인</div>
          <div className="auth-checklist__item">계좌와 잔액 관리</div>
          <div className="auth-checklist__item">환전과 주문 진행</div>
        </div>
      </section>

      <section className="auth-panel">
        <div className="auth-panel__header">
          <h3>로그인</h3>
          <p>이메일과 비밀번호를 입력하세요.</p>
        </div>

        <form onSubmit={onSubmit} className="auth-form">
          <label className="auth-field">
            <span>이메일</span>
            <input type="email" placeholder="user1@example.com" value={email} onChange={e => setEmail(e.target.value)} />
          </label>

          <label className="auth-field">
            <span>비밀번호</span>
            <input type="password" placeholder="Password!" value={password} onChange={e => setPassword(e.target.value)} />
          </label>

          <button type="submit" className="primary-button auth-submit" disabled={loading}>
            {loading ? '로그인 중...' : '로그인'}
          </button>

          {error ? <div className="auth-error">{JSON.stringify(error)}</div> : null}
        </form>

        <div className="auth-footer">
          <span>아직 계정이 없으면</span>
          <Link to="/signup">회원가입</Link>
        </div>
      </section>
    </div>
  )
}
