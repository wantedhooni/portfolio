const escapeRegExp = (value) => value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')

export const getCookie = (name) => {
  if (typeof document === 'undefined') return null
  const match = document.cookie.match(new RegExp(`(?:^|; )${escapeRegExp(name)}=([^;]*)`))
  return match ? decodeURIComponent(match[1]) : null
}

export const setCookie = (name, value, options = {}) => {
  if (typeof document === 'undefined') return
  const { days, path = '/', sameSite = 'Lax', secure } = options
  let cookie = `${name}=${encodeURIComponent(value)}; path=${path}; SameSite=${sameSite}`
  if (typeof days === 'number') {
    cookie += `; Max-Age=${Math.floor(days * 86400)}`
  }
  const shouldSecure = secure ?? (typeof location !== 'undefined' && location.protocol === 'https:')
  if (shouldSecure) cookie += '; Secure'
  document.cookie = cookie
}

export const deleteCookie = (name) => {
  if (typeof document === 'undefined') return
  document.cookie = `${name}=; Max-Age=0; path=/; SameSite=Lax`
}
