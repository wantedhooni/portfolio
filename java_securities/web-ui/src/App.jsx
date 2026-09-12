import React from 'react'
import pkg from '../package.json'
import { Routes, Route, Link, NavLink, useNavigate, Navigate, useLocation } from 'react-router-dom'
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
  const location = useLocation()
  const displayName = auth?.user?.name || auth?.user?.email || '사용자'
  const appEnv = import.meta.env.VITE_TARGET || 'unknown'
  const appVersion = pkg?.version || 'dev'
  const navItems = auth?.user
    ? [
        { to: '/chart', label: '시장' },
        { to: '/account', label: '계좌' },
        { to: '/exchange', label: '환전' },
        { to: '/trade', label: '거래' },
        { to: '/orders', label: '주문' },
      ]
    : []
  const currentLabel =
    navItems.find(item => item.to === location.pathname)?.label ||
    (location.pathname === '/' ? '홈' : location.pathname === '/login' ? '로그인' : location.pathname === '/signup' ? '회원가입' : '고객 포털')

  React.useEffect(() => {
    window.scrollTo({ top: 0, left: 0, behavior: 'auto' })
  }, [location.pathname, location.search])

  function handleLogout() {
    auth.logout()
    navigate('/')
  }

  const ProtectedRoute = ({ children }) => {
    if (!auth?.user) {
      return <Navigate to="/login" replace state={{ from: `${location.pathname}${location.search}` }} />
    }
    return children
  }

  return (
    <div className="app">
      <header className="app-header">
        <div className="app-header__inner">
          <div className="app-brand">
            <Link to="/" className="app-brand__link">
              <span className="app-brand__mark">SM</span>
              <span className="app-brand__text">
                <strong>SM 고객 포털</strong>
                <span>간단한 금융 업무 화면</span>
              </span>
            </Link>
            <span className="app-version">{appEnv} · v{appVersion}</span>
          </div>

          <nav className="app-nav" aria-label="주요 메뉴">
            <NavLink to="/" className={({ isActive }) => `app-nav__link ${isActive ? 'is-active' : ''}`} end>
              홈
            </NavLink>
            {navItems.map(item => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) => `app-nav__link ${isActive ? 'is-active' : ''}`}
              >
                {item.label}
              </NavLink>
            ))}
          </nav>

          <div className="app-actions">
            {auth?.user ? (
              <div className="app-user-chip">
                <span className="app-user-chip__label">사용 중</span>
                <strong>{displayName}</strong>
              </div>
            ) : (
              <div className="app-guest-chip">
                <span>안내</span>
                <strong>로그인 후 전체 메뉴 이용</strong>
              </div>
            )}

            {!auth?.user ? (
              <div className="app-auth-links">
                <Link to="/login" className="ghost-button">로그인</Link>
                <Link to="/signup" className="primary-button">회원가입</Link>
              </div>
            ) : (
              <button onClick={handleLogout} className="logout">로그아웃</button>
            )}

            <ThemeToggle />
          </div>
        </div>
        <div className="app-header__context">
          <div className="app-header__context-inner">
            <div className="app-context">
              <span className="app-context__label">화면</span>
              <strong>{currentLabel}</strong>
            </div>
            <span className="app-context__hint">
              {auth?.user ? '필요한 메뉴만 바로 이용하세요.' : '로그인 후 전체 메뉴를 사용할 수 있습니다.'}
            </span>
          </div>
        </div>
      </header>

      <main className="app-main">
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
