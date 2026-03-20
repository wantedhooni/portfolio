import React, { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthProvider'

export default function Login() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()
  const auth = useAuth()
  const nextPath = location.state?.from || '/'

  const onSubmit = async e => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      await auth.login({ email, password })
      navigate(nextPath, { replace: true })
    } catch (e) {
      setError(e.response?.data || 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-shell auth-shell--compact">
      <section className="auth-panel">
        <div className="auth-panel__header">
          <h3>로그인</h3>
          <p>이메일과 비밀번호를 입력하세요. 테스트 계정은 `user1@example.com` / `Password!` 입니다.</p>
        </div>

        <form onSubmit={onSubmit} className="auth-form">
          <label className="auth-field">
            <span>이메일</span>
            <input type="email" autoComplete="email" placeholder="user1@example.com" value={email} onChange={e => setEmail(e.target.value)} />
          </label>

          <label className="auth-field">
            <span>비밀번호</span>
            <input type="password" autoComplete="current-password" placeholder="Password!" value={password} onChange={e => setPassword(e.target.value)} />
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
