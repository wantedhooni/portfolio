import React, { createContext, useContext, useEffect, useState } from 'react'
import { login as apiLogin, signup as apiSignup } from '../api/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)

  useEffect(() => {
    const token = localStorage.getItem('access_token')
    const cachedProfile = localStorage.getItem('user_profile')
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

  async function login(payload) {
    const res = await apiLogin(payload)
    const profile = {
      authenticated: true,
      name: res?.user?.name || res?.name || res?.userName || res?.username || payload?.name || payload?.email,
      email: res?.user?.email || res?.email || payload?.email,
    }
    setUser(profile)
    try {
      localStorage.setItem('user_profile', JSON.stringify(profile))
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
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
    localStorage.removeItem('user_profile')
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
