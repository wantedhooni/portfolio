import React from 'react'
import { useTheme } from '../theme/ThemeProvider'

export default function ThemeToggle() {
  const { theme, setTheme } = useTheme()
  const toggle = () => setTheme(theme === 'light' ? 'dark' : 'light')

  return (
    <button className="theme-toggle" onClick={toggle} aria-label={theme === 'light' ? 'Switch to dark' : 'Switch to light'}>      
      {theme === 'light' ? '🌙' : '☀️'}
    </button>
  )
}
