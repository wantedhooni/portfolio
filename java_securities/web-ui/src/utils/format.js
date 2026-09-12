export function formatApiError(error, fallback = '요청을 처리하지 못했습니다.') {
  if (!error) return fallback
  if (typeof error === 'string') return error
  if (Array.isArray(error)) return error.map(item => formatApiError(item, fallback)).join(', ')
  if (typeof error === 'object') {
    if (typeof error.message === 'string' && error.message.trim()) return error.message
    if (typeof error.error === 'string' && error.error.trim()) return error.error
    if (typeof error.detail === 'string' && error.detail.trim()) return error.detail
    if (typeof error.title === 'string' && error.title.trim()) return error.title
    if (Array.isArray(error.errors)) return error.errors.map(item => formatApiError(item, fallback)).join(', ')
  }
  return fallback
}

export function formatAccountStatus(status) {
  const labels = {
    ACTIVE: '정상',
    SUSPENDED: '거래 제한',
    CLOSED: '해지',
  }
  return labels[status] || status || '-'
}

export function formatOrderStatus(status) {
  const labels = {
    NEW: '접수',
    PARTIALLY_FILLED: '부분 체결',
    FILLED: '체결 완료',
    CANCELED: '취소됨',
    REJECTED: '거절됨',
  }
  return labels[status] || status || '-'
}
