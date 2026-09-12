import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthProvider'
import { formatApiError } from '../utils/format'

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
      setError(formatApiError(err.response?.data || err.message, '회원가입에 실패했습니다.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-shell auth-shell--compact">
      <section className="auth-panel">
        <div className="auth-panel__header">
          <h3>회원가입</h3>
          <p>간단한 사용자 정보를 입력해 계정을 만드세요.</p>
        </div>

        <form onSubmit={onSubmit} className="auth-form">
          <label className="auth-field">
            <span>이름</span>
            <input type="text" autoComplete="name" placeholder="홍길동" value={name} onChange={e => setName(e.target.value)} />
          </label>

          <label className="auth-field">
            <span>이메일</span>
            <input type="email" autoComplete="email" placeholder="demo@example.com" value={email} onChange={e => setEmail(e.target.value)} />
          </label>

          <label className="auth-field">
            <span>비밀번호</span>
            <input type="password" autoComplete="new-password" placeholder="8자 이상 입력" value={password} onChange={e => setPassword(e.target.value)} />
          </label>

          <label className="auth-field">
            <span>전화번호</span>
            <input type="text" autoComplete="tel" placeholder="010-1234-5678" value={phone} onChange={e => setPhone(e.target.value)} />
          </label>

          <label className="auth-field">
            <span>주소</span>
            <input type="text" autoComplete="street-address" placeholder="서울시 ..." value={address} onChange={e => setAddress(e.target.value)} />
          </label>

          <button type="submit" className="primary-button auth-submit" disabled={loading}>
            {loading ? '가입 처리 중...' : '회원가입'}
          </button>

          {error ? <div className="auth-error">{error}</div> : null}
        </form>

        <div className="auth-footer">
          <span>이미 계정이 있으면</span>
          <Link to="/login">로그인</Link>
        </div>
      </section>
    </div>
  )
}
