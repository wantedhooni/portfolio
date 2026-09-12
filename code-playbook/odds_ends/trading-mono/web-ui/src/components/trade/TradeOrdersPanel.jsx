import React from 'react'

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
      <section className="orders-header">
          <div>
            <h2>주문 조회</h2>
            <p>최근 주문 내역을 확인하고 취소할 수 있습니다.</p>
          </div>
        </section>
      <section className="orders-header">
        
        <div className="orders-actions">
          <div className="orders-size">
            <label>행</label>
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
      </section>

      {ordersError && <div className="trade-alert is-error">{JSON.stringify(ordersError)}</div>}
      {ordersMessage && <div className="trade-alert">{ordersMessage}</div>}

      <section className="orders-card">
        {orders.length === 0 ? (
          <div className="trade-empty">주문 내역이 없습니다.</div>
        ) : (
          <table className="orders-table">
            <thead>
              <tr>
                <th>액션</th>
                <th>주문 ID</th>
                <th>계좌</th>
                <th>심볼</th>
                <th>구분</th>
                <th>유형</th>
                <th>상태</th>
                <th>수량</th>
                <th>지정가</th>
                <th>통화</th>
              </tr>
            </thead>
            <tbody>
              {orders.map(order => (
                <tr key={order.id}>
                  <td>
                    {cancelable.has(order.status) ? (
                      <button className="ghost-button" onClick={() => onCancel(order.id)}>
                        취소
                      </button>
                    ) : (
                      <span className="orders-muted">-</span>
                    )}
                  </td>
                  <td>{order.id}</td>
                  <td>{order.accountNo}</td>
                  <td>{order.symbol}</td>
                  <td>{order.side}</td>
                  <td>{order.type}</td>
                  <td>{order.status}</td>
                  <td>{order.qty}</td>
                  <td>{order.limitPriceAmount ?? '-'}</td>
                  <td>{order.priceCurrency ?? '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </section>
  )
}
