import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthProvider'

export default function Login() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)
  const navigate = useNavigate()
  const auth = useAuth()

  const onSubmit = async e => {
    e.preventDefault()
    try {
      await auth.login({ email, password })
      navigate('/')
    } catch (e) {
      setError(e.response?.data || 'Login failed')
    }
  }

  return (
    <div className="container">
      <form onSubmit={onSubmit} className="form-row" style={{ flexDirection: 'column', gap: 8 }}>
        <input type="email" placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} />
        <input type="password" placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} />
        <button type="submit">Login</button>
        {error && <div style={{ color: 'red' }}>{JSON.stringify(error)}</div>}
      </form>
    </div>
  )
}
