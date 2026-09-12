import React from 'react'
// Set React Router v7 future flags to opt-in early and reduce console warnings
// See: reactrouter.com/v6/upgrading/future#v7_starttransition
if (typeof window !== 'undefined') {
  try {
    window.__react_router_future__ = window.__react_router_future__ || {}
    window.__react_router_future__.v7_startTransition = true
    window.__react_router_future__.v7_relativeSplatPath = true
  } catch (e) {
    // ignore
  }
  // prevent noisy unhandled promise rejections from surfacing as fatal console errors
  window.addEventListener('unhandledrejection', (ev) => {
    // keep visible in dev console but avoid uncaught red error overlay
    console.warn('Unhandled promise rejection:', ev.reason)
    ev.preventDefault()
  })
}

// Diagnostic: log cross-window messages to help identify message-port errors
if (typeof window !== 'undefined') {
  window.addEventListener('message', (ev) => {
    // log origin and data summary
    try {
      const s = typeof ev.data === 'string' ? ev.data : JSON.stringify(ev.data).slice(0, 200)
      console.debug('[message event] origin=', ev.origin, ' data=', s)
    } catch (e) {
      console.debug('[message event] origin=', ev.origin, ' (unserializable data)')
    }
  })

  window.addEventListener('messageerror', (ev) => {
    console.warn('messageerror event:', ev)
  })
}
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App'
import { AuthProvider } from './auth/AuthProvider'
import { ThemeProvider } from './theme/ThemeProvider'
import './styles.css'

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <ThemeProvider>
        <AuthProvider>
          <App />
        </AuthProvider>
      </ThemeProvider>
    </BrowserRouter>
  </React.StrictMode>
)
