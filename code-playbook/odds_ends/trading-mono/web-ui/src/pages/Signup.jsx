import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
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
    <div className="container">
      <h2>회원가입</h2>
      <form onSubmit={onSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
        <input type="text" placeholder="Name" value={name} onChange={e => setName(e.target.value)} />
        <input type="email" placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} />
        <input type="password" placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} />
        <input type="text" placeholder="Phone" value={phone} onChange={e => setPhone(e.target.value)} />
        <input type="text" placeholder="Address" value={address} onChange={e => setAddress(e.target.value)} />
        <button type="submit" disabled={loading}>{loading ? '로딩...' : '회원가입'}</button>
        {error && <div style={{ color: 'red' }}>{JSON.stringify(error)}</div>}
      </form>
    </div>
  )
}
