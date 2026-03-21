import React from 'react'

export default function TradeAccountsCard({
  currency,
  totalAvailable,
  accounts,
  selectedAccountNo,
  onSelectAccount,
  formatAmount,
}) {
  return (
    <div className="trade-card">
      <div className="trade-card__title-row">
        <div>
          <h3>주문 가능한 계좌</h3>
          <p className="trade-card__caption">현재 종목 통화와 같은 계좌만 먼저 보여줍니다.</p>
        </div>
      </div>
      {currency ? (
        <div className="trade-balance">
          <div className="trade-balance__total">
            <span>사용 가능 합계</span>
            <strong>{formatAmount(totalAvailable)}</strong>
          </div>
          {accounts.length === 0 ? (
            <div className="trade-empty">해당 통화 계좌가 없습니다.</div>
          ) : (
            <ul className="trade-account-list">
              {accounts.map(account => (
                <li key={account.accountNo}>
                  <button
                    type="button"
                    className={`trade-account-item ${selectedAccountNo === account.accountNo ? 'is-selected' : ''}`}
                    onClick={() => onSelectAccount(account.accountNo)}
                  >
                    <div className="trade-account-item__meta">
                      <strong>{account.accountNo}</strong>
                      <span>{account.type}</span>
                    </div>
                    <div className="trade-account-item__amount">
                      <span>주문 가능 금액</span>
                      <strong>{formatAmount(account.availableCash)}</strong>
                    </div>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      ) : (
        <div className="trade-empty">통화 정보를 확인할 수 없습니다.</div>
      )}
    </div>
  )
}
