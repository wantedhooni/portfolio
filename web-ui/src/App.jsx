import React from 'react'
import pkg from '../package.json'
import { Routes, Route, Link, useNavigate, Navigate } from 'react-router-dom'
import Home from './pages/Home'
import Login from './pages/Login'
import Signup from './pages/Signup'
import Chart from './pages/Chart'
import Account from './pages/Account'
import Exchange from './pages/Exchange'
import Trade from './pages/Trade'
import Orders from './pages/Orders'
import { useAuth } from './auth/AuthProvider'
import ThemeToggle from './components/ThemeToggle'

export default function App() {
  const auth = useAuth()
  const navigate = useNavigate()
  const displayName = auth?.user?.name || auth?.user?.email || '사용자'
  const appEnv = import.meta.env.VITE_TARGET || 'unknown'
  const appVersion = pkg?.version || 'dev'
  
  

  function handleLogout() {
    auth.logout()
    navigate('/')
  }

  const ProtectedRoute = ({ children }) => {
    if (!auth?.user) return <Navigate to="/" replace />
    return children
  }

  return (
    <div className="app">
      <header className="app-header">
        <div className="app-title">
          <h1>Trading Charts</h1>
          <span className="app-version">{appEnv}(v{appVersion})</span>
        </div>
        <nav className="nav-right">
          <Link to="/">Home</Link>
          
          {!auth?.user ? (
            <>
              <Link to="/login">Login</Link>
              <Link to="/signup">Signup</Link>
            </>
          ) : (
            <>
              <Link to="/chart">Chart</Link>  
              <Link to="/account">Account</Link>  
              <Link to="/exchange">exchange</Link>  
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
          <Route path="/chart" element={<ProtectedRoute><Chart /></ProtectedRoute>} />
          <Route path="/account" element={<ProtectedRoute><Account /></ProtectedRoute>} />
          <Route path="/exchange" element={<ProtectedRoute><Exchange /></ProtectedRoute>} />
          <Route path="/trade" element={<ProtectedRoute><Trade /></ProtectedRoute>} />
          <Route path="/orders" element={<ProtectedRoute><Orders /></ProtectedRoute>} />
        </Routes>
      </main>
    </div>
  )
}
