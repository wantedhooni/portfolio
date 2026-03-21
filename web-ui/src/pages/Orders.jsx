import React, { useEffect, useState } from 'react'
import { cancelOrder, getOrders } from '../api/api'
import { formatApiError, formatOrderStatus } from '../utils/format'

const CANCELABLE = new Set(['NEW', 'PARTIALLY_FILLED'])

export default function Orders() {
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(20)
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [actionMessage, setActionMessage] = useState(null)

  const fetchOrders = async (nextPage = page) => {
    setLoading(true)
    setError(null)
    setActionMessage(null)
    try {
      const res = await getOrders(nextPage, size)
      setData(res)
    } catch (e) {
      setError(formatApiError(e.response?.data || e.message, '주문 내역을 불러오지 못했습니다.'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchOrders(page)
  }, [page, size])

  const handleCancel = async orderId => {
    setActionMessage(null)
    setError(null)
    try {
      await cancelOrder(orderId)
      setActionMessage('주문이 취소되었습니다.')
      await fetchOrders(page)
    } catch (e) {
      setError(formatApiError(e.response?.data || e.message, '주문 취소에 실패했습니다.'))
    }
  }

  const orders = data?.content || []
  const totalPages = data?.totalPages ?? 0
  const totalElements = data?.totalElements ?? orders.length
  const activeOrders = orders.filter(order => CANCELABLE.has(order.status)).length
  const filledOrders = orders.filter(order => order.status === 'FILLED').length
  const latestOrder = orders[0] || null

  return (
    <div className="orders-page">
      <section className="orders-summary">
        <div className="orders-summary__item">
          <span>이번 조회 건수</span>
          <strong>{orders.length}</strong>
        </div>
        <div className="orders-summary__item">
          <span>취소 가능</span>
          <strong>{activeOrders}</strong>
        </div>
        <div className="orders-summary__item">
          <span>체결 완료</span>
          <strong>{filledOrders}</strong>
        </div>
        <div className="orders-summary__item">
          <span>전체 주문 수</span>
          <strong>{totalElements}</strong>
        </div>
      </section>

      <section className="orders-header">
        <div>
          <h2>주문 조회</h2>
          <p>주문 내역을 확인하고 취소 가능한 주문만 바로 처리할 수 있습니다.</p>
        </div>
      </section>
      <div className="orders-toolbar">
        <div className="orders-size">
          <label>페이지 크기</label>
          <select value={size} onChange={e => setSize(Number(e.target.value))}>
            {[10, 20, 30, 50].map(opt => (
              <option key={opt} value={opt}>{opt}</option>
            ))}
          </select>
        </div>
        <button className="ghost-button" onClick={() => fetchOrders(page)} disabled={loading}>
          {loading ? '불러오는 중...' : '새로고침'}
        </button>
      </div>

      {latestOrder ? (
        <section className="orders-highlight">
          <div>
            <span>가장 최근 주문</span>
            <strong>{latestOrder.symbol} · {latestOrder.accountNo}</strong>
          </div>
          <div>
            <span>상태</span>
            <strong>{formatOrderStatus(latestOrder.status)}</strong>
          </div>
          <div>
            <span>수량</span>
            <strong>{latestOrder.qty}</strong>
          </div>
        </section>
      ) : null}

      {error && <div className="trade-alert is-error">{error}</div>}
      {actionMessage && <div className="trade-alert">{actionMessage}</div>}

      <section className="orders-card">
        {orders.length === 0 ? (
          <div className="trade-empty">주문 내역이 없습니다.</div>
        ) : (
          <div className="orders-list">
            {orders.map(order => (
              <article key={order.id} className="orders-list-item">
                <div className="orders-list-item__header">
                  <div>
                    <strong>{order.symbol}</strong>
                    <p>{order.accountNo} · 주문 ID {order.id}</p>
                  </div>
                  <span className={`order-status-badge order-status-badge--${String(order.status || '').toLowerCase()}`}>
                    {formatOrderStatus(order.status)}
                  </span>
                </div>
                <div className="orders-list-item__grid">
                  <div>
                    <span>구분</span>
                    <strong>{order.side}</strong>
                  </div>
                  <div>
                    <span>유형</span>
                    <strong>{order.type}</strong>
                  </div>
                  <div>
                    <span>수량</span>
                    <strong>{order.qty}</strong>
                  </div>
                  <div>
                    <span>지정가</span>
                    <strong>{order.limitPriceAmount ?? '-'}</strong>
                  </div>
                  <div>
                    <span>통화</span>
                    <strong>{order.priceCurrency ?? '-'}</strong>
                  </div>
                </div>
                <div className="orders-list-item__footer">
                  <span className="orders-muted">
                    {CANCELABLE.has(order.status) ? '지금 취소할 수 있는 주문입니다.' : '조회 전용 주문입니다.'}
                  </span>
                  {CANCELABLE.has(order.status) ? (
                    <button className="ghost-button" onClick={() => handleCancel(order.id)}>
                      주문 취소
                    </button>
                  ) : null}
                </div>
              </article>
            ))}
          </div>
        )}
      </section>

      <section className="orders-pagination">
        <button
          className="ghost-button"
          onClick={() => setPage(p => Math.max(0, p - 1))}
          disabled={page === 0 || loading}
        >
          이전
        </button>
        <span>
          {page + 1} / {Math.max(1, totalPages)}
        </span>
        <button
          className="ghost-button"
          onClick={() => setPage(p => (totalPages ? Math.min(totalPages - 1, p + 1) : p + 1))}
          disabled={totalPages ? page >= totalPages - 1 : loading}
        >
          다음
        </button>
      </section>
    </div>
  )
}
