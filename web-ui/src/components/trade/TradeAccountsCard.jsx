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
      <h3>동일 통화 계좌 잔고</h3>
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
                    <div>
                      <strong>{account.accountNo}</strong>
                      <span>{account.type}</span>
                    </div>
                    <div>
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
