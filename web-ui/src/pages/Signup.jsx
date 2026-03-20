import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthProvider'

export default function Signup() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [name, setName] = useState('')
  const [phone, setPhone] = useState('')
  const [address, setAddress] = useState('')
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)
  const auth = useAuth()
  const navigate = useNavigate()

  const onSubmit = async e => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      await auth.signup({ email, password, name, phone, address })
      navigate('/login')
    } catch (err) {
      setError(err.response?.data || 'Signup failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-shell auth-shell--wide">
      <section className="auth-panel auth-panel--feature">
        <span className="intro-eyebrow">Create Account</span>
        <h2>사용자 계정 생성</h2>
        <p className="auth-lead">
          간단한 사용자 정보를 입력하면 바로 로그인 후 기능을 사용할 수 있습니다.
        </p>

        <div className="auth-checklist">
          <div className="auth-checklist__item">기본 정보 입력</div>
          <div className="auth-checklist__item">가입 후 로그인 이동</div>
          <div className="auth-checklist__item">이후 계좌와 거래 기능 사용</div>
        </div>
      </section>

      <section className="auth-panel">
        <div className="auth-panel__header">
          <h3>회원가입</h3>
          <p>간단한 사용자 정보를 입력해 데모 계정을 만드세요.</p>
        </div>

        <form onSubmit={onSubmit} className="auth-form">
          <label className="auth-field">
            <span>이름</span>
            <input type="text" placeholder="홍길동" value={name} onChange={e => setName(e.target.value)} />
          </label>

          <label className="auth-field">
            <span>이메일</span>
            <input type="email" placeholder="demo@example.com" value={email} onChange={e => setEmail(e.target.value)} />
          </label>

          <label className="auth-field">
            <span>비밀번호</span>
            <input type="password" placeholder="8자 이상 입력" value={password} onChange={e => setPassword(e.target.value)} />
          </label>

          <label className="auth-field">
            <span>전화번호</span>
            <input type="text" placeholder="010-1234-5678" value={phone} onChange={e => setPhone(e.target.value)} />
          </label>

          <label className="auth-field">
            <span>주소</span>
            <input type="text" placeholder="서울시 ..." value={address} onChange={e => setAddress(e.target.value)} />
          </label>

          <button type="submit" className="primary-button auth-submit" disabled={loading}>
            {loading ? '가입 처리 중...' : '회원가입'}
          </button>

          {error ? <div className="auth-error">{JSON.stringify(error)}</div> : null}
        </form>

        <div className="auth-footer">
          <span>이미 계정이 있으면</span>
          <Link to="/login">로그인</Link>
        </div>
      </section>
    </div>
  )
}
