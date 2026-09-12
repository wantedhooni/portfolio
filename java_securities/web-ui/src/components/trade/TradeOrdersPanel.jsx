import React from 'react'
import { formatOrderStatus } from '../../utils/format'

export default function TradeOrdersPanel({
  orders,
  ordersLoading,
  ordersError,
  ordersMessage,
  ordersSize,
  ordersStatus,
  onChangeSize,
  onChangeStatus,
  onRefresh,
  onCancel,
}) {
  const cancelable = new Set(['NEW', 'PARTIALLY_FILLED'])

  return (
    <section className="orders-embed trade-card">
      <section className="trade-card__title-row">
        <div>
          <h3>최근 주문</h3>
          <p className="trade-card__caption">방금 넣은 주문 상태만 빠르게 확인할 수 있게 정리했습니다.</p>
        </div>
      </section>
      <div className="orders-toolbar">
        <div className="orders-size">
          <label>행 수</label>
          <select value={ordersSize} onChange={e => onChangeSize(Number(e.target.value))}>
            {[10, 20, 30, 50].map(opt => (
              <option key={opt} value={opt}>{opt}</option>
            ))}
          </select>
        </div>
        <div className="orders-size">
          <label>상태</label>
          <select value={ordersStatus} onChange={e => onChangeStatus(e.target.value)}>
            <option value="">전체</option>
            <option value="NEW">NEW</option>
            <option value="PARTIALLY_FILLED">PARTIALLY_FILLED</option>
            <option value="FILLED">FILLED</option>
            <option value="CANCELED">CANCELED</option>
            <option value="REJECTED">REJECTED</option>
          </select>
        </div>
        <button className="ghost-button" onClick={onRefresh} disabled={ordersLoading}>
          {ordersLoading ? '불러오는 중...' : '새로고침'}
        </button>
      </div>

      {ordersError && <div className="trade-alert is-error">{ordersError}</div>}
      {ordersMessage && <div className="trade-alert">{ordersMessage}</div>}

      <section className="orders-card">
        {orders.length === 0 ? (
          <div className="trade-empty trade-empty--boxed">주문 내역이 없습니다.</div>
        ) : (
          <div className="orders-list orders-list--compact">
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
                </div>
                <div className="orders-list-item__footer">
                  <span className="orders-muted">
                    {cancelable.has(order.status) ? '취소 가능한 주문입니다.' : '상태만 확인할 수 있습니다.'}
                  </span>
                  {cancelable.has(order.status) ? (
                    <button className="ghost-button" onClick={() => onCancel(order.id)}>
                      취소
                    </button>
                  ) : null}
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </section>
  )
}
