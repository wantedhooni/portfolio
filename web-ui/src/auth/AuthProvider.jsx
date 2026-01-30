import React, { createContext, useContext, useEffect, useState } from 'react'
import { login as apiLogin, signup as apiSignup } from '../api/api'
import { getCookie, setCookie, deleteCookie } from '../utils/cookies'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)

  useEffect(() => {
    const token = getCookie('access_token')
    const cachedProfile = getCookie('user_profile')
    if (cachedProfile) {
      try {
        setUser(JSON.parse(cachedProfile))
        return
      } catch (e) {
        // ignore parse errors
      }
    }
    if (token) setUser({ authenticated: true })
  }, [])

  useEffect(() => {
    const handleLogout = () => logout()
    window.addEventListener('auth:logout', handleLogout)
    return () => window.removeEventListener('auth:logout', handleLogout)
  }, [])

  async function login(payload) {
    const res = await apiLogin(payload)
    const profile = {
      authenticated: true,
      name: res?.user?.name || res?.name || res?.userName || res?.username || payload?.name || payload?.email,
      email: res?.user?.email || res?.email || payload?.email,
    }
    setUser(profile)
    try {
      setCookie('user_profile', JSON.stringify(profile))
    } catch (e) {
      // ignore storage issues
    }
    return res
  }

  async function signup(payload) {
    const res = await apiSignup(payload)
    return res
  }

  function logout() {
    deleteCookie('access_token')
    deleteCookie('refresh_token')
    deleteCookie('user_profile')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, login, signup, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
