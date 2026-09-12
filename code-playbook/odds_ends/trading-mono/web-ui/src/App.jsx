import React from 'react'
import { Routes, Route, Link, useNavigate } from 'react-router-dom'
import Home from './pages/Home'
import Login from './pages/Login'
import Signup from './pages/Signup'
import Market from './pages/Market'
import Account from './pages/Account'
import Trade from './pages/Trade'
import Orders from './pages/Orders'
import { useAuth } from './auth/AuthProvider'
import ThemeToggle from './components/ThemeToggle'

export default function App() {
  const auth = useAuth()
  const navigate = useNavigate()
  const displayName = auth?.user?.name || auth?.user?.email || '사용자'

  function handleLogout() {
    auth.logout()
    navigate('/login')
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>Trading Charts</h1>
        <nav className="nav-right">
          <Link to="/">Home</Link>
          <Link to="/market">Market</Link>
          {!auth?.user ? (
            <>
              <Link to="/login">Login</Link>
              <Link to="/signup">Signup</Link>
            </>
          ) : (
            <>
              <Link to="/account">Account</Link>
              <Link to="/trade">Trade</Link>
              <span className="greeting">{displayName} 님, 안녕하세요 </span>
              <button onClick={handleLogout} className="logout">Logout</button>
            </>
          )}

          <ThemeToggle />
        </nav>
      </header>

      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          <Route path="/market" element={<Market />} />
          <Route path="/account" element={<Account />} />
          <Route path="/trade" element={<Trade />} />
          <Route path="/orders" element={<Orders />} />
        </Routes>
      </main>
    </div>
  )
}
