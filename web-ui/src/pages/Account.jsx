import React, { useCallback, useEffect, useMemo, useState } from 'react'
import {
  createAccount,
  depositAccount,
  getMyAccounts,
  transferAccount,
  withdrawAccount,
} from '../api/api'
import { useAuth } from '../auth/AuthProvider'

const ACCOUNT_TYPES = ['CASH', 'MARGIN']
const ACCOUNT_STATUSES = ['ACTIVE', 'SUSPENDED', 'CLOSED']

function parseCsv(value) {
  return value
    .split(',')
    .map(v => v.trim())
    .filter(Boolean)
}

export default function Account() {
  const auth = useAuth()
  const [accounts, setAccounts] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [filters, setFilters] = useState({
    currencies: '',
    types: ['CASH', 'MARGIN'],
    statuses: ['ACTIVE'],
  })

  const [createForm, setCreateForm] = useState({ accountType: 'CASH', currency: 'USD' })
  const [depositForm, setDepositForm] = useState({ accountNo: '', amount: '' })
  const [withdrawForm, setWithdrawForm] = useState({ accountNo: '', amount: '' })
  const [transferForm, setTransferForm] = useState({
    fromAccountNo: '',
    toAccountNo: '',
    amount: '',
    referenceId: '',
    fromDescription: '',
    toDescription: '',
  })
  const [actionMessage, setActionMessage] = useState(null)
  const [actionError, setActionError] = useState(null)
  const activeAccounts = useMemo(
    () => accounts.filter(account => account.status === 'ACTIVE'),
    [accounts]
  )
  const totalAvailable = useMemo(
    () => accounts.reduce((sum, account) => sum + (Number(account.availableCash) || 0), 0),
    [accounts]
  )
  const accountCurrencies = useMemo(
    () => Array.from(new Set(accounts.map(account => account.currency).filter(Boolean))),
    [accounts]
  )

  const filterPayload = useMemo(() => {
    return {
      currencies: parseCsv(filters.currencies),
      types: filters.types,
      statuses: filters.statuses,
    }
  }, [filters])

  const fetchAccounts = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await getMyAccounts(filterPayload)
      setAccounts(Array.isArray(data) ? data : [])
    } catch (e) {
      setError(e.response?.data || e.message)
    } finally {
      setLoading(false)
    }
  }, [filterPayload])

  useEffect(() => {
    fetchAccounts()
  }, [fetchAccounts])

  const handleCreate = async e => {
    e.preventDefault()
    setActionMessage(null)
    setActionError(null)
    try {
      const payload = {
        accountType: createForm.accountType,
        currency: createForm.currency.trim(),
      }
      const res = await createAccount(payload)
      setActionMessage(`계좌가 생성되었습니다. ${res?.accountNo || ''}`.trim())
      setCreateForm(prev => ({ ...prev, currency: '' }))
      await fetchAccounts()
    } catch (e) {
      setActionError(e.response?.data || e.message)
    }
  }

  const handleDeposit = async e => {
    e.preventDefault()
    setActionMessage(null)
    setActionError(null)
    try {
      const payload = {
        accountNo: depositForm.accountNo.trim(),
        amount: Number(depositForm.amount),
      }
      await depositAccount(payload)
      setActionMessage('입금이 완료되었습니다.')
      setDepositForm({ accountNo: '', amount: '' })
      await fetchAccounts()
    } catch (e) {
      setActionError(e.response?.data || e.message)
    }
  }

  const handleWithdraw = async e => {
    e.preventDefault()
    setActionMessage(null)
    setActionError(null)
    try {
      const payload = {
        accountNo: withdrawForm.accountNo.trim(),
        amount: Number(withdrawForm.amount),
      }
      await withdrawAccount(payload)
      setActionMessage('출금이 완료되었습니다.')
      setWithdrawForm({ accountNo: '', amount: '' })
      await fetchAccounts()
    } catch (e) {
      setActionError(e.response?.data || e.message)
    }
  }

  const handleTransfer = async e => {
    e.preventDefault()
    setActionMessage(null)
    setActionError(null)
    try {
      const payload = {
        fromAccountNo: transferForm.fromAccountNo.trim(),
        toAccountNo: transferForm.toAccountNo.trim(),
        amount: Number(transferForm.amount),
        referenceId: transferForm.referenceId.trim() || undefined,
        fromDescription: transferForm.fromDescription.trim() || undefined,
        toDescription: transferForm.toDescription.trim() || undefined,
      }
      await transferAccount(payload)
      setActionMessage('계좌 이체가 완료되었습니다.')
      setTransferForm({
        fromAccountNo: '',
        toAccountNo: '',
        amount: '',
        referenceId: '',
        fromDescription: '',
        toDescription: '',
      })
      await fetchAccounts()
    } catch (e) {
      setActionError(e.response?.data || e.message)
    }
  }

  const toggleFilter = (key, value) => {
    setFilters(prev => {
      const list = new Set(prev[key])
      if (list.has(value)) list.delete(value)
      else list.add(value)
      return { ...prev, [key]: Array.from(list) }
    })
  }

  return (
    <div className="account-page">
      <section className="account-hero">
        <div>
          <h2>Account</h2>
          <p className="account-subtitle">계좌 현황 확인과 거래를 한 번에 관리하세요.</p>
        </div>
        <div className="account-hero__meta">
          {auth?.user ? <span>로그인 상태</span> : <span>로그인이 필요합니다</span>}
          <button onClick={fetchAccounts} className="ghost-button" disabled={loading}>
            {loading ? '불러오는 중...' : '새로고침'}
          </button>
        </div>
      </section>

      <section className="account-summary">
        <div className="account-summary__card">
          <span>전체 계좌 수</span>
          <strong>{accounts.length}</strong>
        </div>
        <div className="account-summary__card">
          <span>활성 계좌</span>
          <strong>{activeAccounts.length}</strong>
        </div>
        <div className="account-summary__card">
          <span>사용 가능 금액 합계</span>
          <strong>{totalAvailable.toLocaleString('en-US')}</strong>
        </div>
        <div className="account-summary__card">
          <span>보유 통화</span>
          <strong>{accountCurrencies.join(', ') || '-'}</strong>
        </div>
      </section>

      <section className="account-filters">
        <div className="account-filter">
          <label>통화</label>
          <input
            type="text"
            value={filters.currencies}
            onChange={e => setFilters(prev => ({ ...prev, currencies: e.target.value }))}
            placeholder="예: USD, KRW"
          />
        </div>
        <div className="account-filter">
          <label>계좌 유형</label>
          <div className="filter-chips">
            {ACCOUNT_TYPES.map(type => (
              <button
                key={type}
                type="button"
                className={`chip-button ${filters.types.includes(type) ? 'is-active' : ''}`}
                onClick={() => toggleFilter('types', type)}
              >
                {type}
              </button>
            ))}
          </div>
        </div>
        <div className="account-filter">
          <label>상태</label>
          <div className="filter-chips">
            {ACCOUNT_STATUSES.map(status => (
              <button
                key={status}
                type="button"
                className={`chip-button ${filters.statuses.includes(status) ? 'is-active' : ''}`}
                onClick={() => toggleFilter('statuses', status)}
              >
                {status}
              </button>
            ))}
          </div>
        </div>
      </section>

      {error && <div className="account-alert is-error">{JSON.stringify(error)}</div>}
      {actionMessage && <div className="account-alert is-success">{actionMessage}</div>}
      {actionError && <div className="account-alert is-error">{JSON.stringify(actionError)}</div>}

      <section className="account-grid">
        {accounts.length === 0 ? (
          <div className="account-empty">표시할 계좌가 없습니다.</div>
        ) : (
          accounts.map(account => (
            <article key={account.accountNo} className="account-card">
              <div className="account-card__header">
                <div>
                  <h3>{account.accountNo}</h3>
                  <p>{account.currency} · {account.type}</p>
                </div>
                <span className={`account-status account-status--${account.status?.toLowerCase() || 'active'}`}>
                  {account.status}
                </span>
              </div>
              <div className="account-card__body">
                <div>
                  <span>Cash Balance</span>
                  <strong>{account.cashBalance}</strong>
                </div>
                <div>
                  <span>Available</span>
                  <strong>{account.availableCash}</strong>
                </div>
              </div>
            </article>
          ))
        )}
      </section>

      <section className="account-actions">
        <form className="account-panel" onSubmit={handleCreate}>
          <h3>계좌 개설</h3>
          <label>계좌 유형</label>
          <select
            value={createForm.accountType}
            onChange={e => setCreateForm(prev => ({ ...prev, accountType: e.target.value }))}
          >
            {ACCOUNT_TYPES.map(type => (
              <option key={type} value={type}>{type}</option>
            ))}
          </select>
          <label>통화</label>
          <input
            type="text"
            value={createForm.currency}
            onChange={e => setCreateForm(prev => ({ ...prev, currency: e.target.value }))}
            placeholder="USD"
          />
          <button type="submit" className="primary-button">개설</button>
        </form>

        <form className="account-panel" onSubmit={handleDeposit}>
          <h3>입금</h3>
          <label>계좌번호</label>
          <select
            value={depositForm.accountNo}
            onChange={e => setDepositForm(prev => ({ ...prev, accountNo: e.target.value }))}
          >
            <option value="">계좌 선택</option>
            {activeAccounts.map(account => (
              <option key={account.accountNo} value={account.accountNo}>
                {account.accountNo} · {account.currency}
              </option>
            ))}
          </select>
          <label>금액</label>
          <input
            type="number"
            value={depositForm.amount}
            onChange={e => setDepositForm(prev => ({ ...prev, amount: e.target.value }))}
            placeholder="1000"
          />
          <button type="submit" className="primary-button">입금</button>
        </form>

        <form className="account-panel" onSubmit={handleWithdraw}>
          <h3>출금</h3>
          <label>계좌번호</label>
          <select
            value={withdrawForm.accountNo}
            onChange={e => setWithdrawForm(prev => ({ ...prev, accountNo: e.target.value }))}
          >
            <option value="">계좌 선택</option>
            {activeAccounts.map(account => (
              <option key={account.accountNo} value={account.accountNo}>
                {account.accountNo} · {account.currency}
              </option>
            ))}
          </select>
          <label>금액</label>
          <input
            type="number"
            value={withdrawForm.amount}
            onChange={e => setWithdrawForm(prev => ({ ...prev, amount: e.target.value }))}
            placeholder="1000"
          />
          <button type="submit" className="primary-button">출금</button>
        </form>

        <form className="account-panel account-panel--wide" onSubmit={handleTransfer}>
          <h3>계좌 이체</h3>
          <div className="account-row">
            <div>
              <label>출금 계좌</label>
              <select
                value={transferForm.fromAccountNo}
                onChange={e => setTransferForm(prev => ({ ...prev, fromAccountNo: e.target.value }))}
              >
                <option value="">출금 계좌 선택</option>
                {activeAccounts.map(account => (
                  <option key={account.accountNo} value={account.accountNo}>
                    {account.accountNo} · {account.currency}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label>입금 계좌</label>
              <select
                value={transferForm.toAccountNo}
                onChange={e => setTransferForm(prev => ({ ...prev, toAccountNo: e.target.value }))}
              >
                <option value="">입금 계좌 선택</option>
                {activeAccounts.map(account => (
                  <option key={account.accountNo} value={account.accountNo}>
                    {account.accountNo} · {account.currency}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div className="account-row">
            <div>
              <label>금액</label>
              <input
                type="number"
                value={transferForm.amount}
                onChange={e => setTransferForm(prev => ({ ...prev, amount: e.target.value }))}
                placeholder="1000"
              />
            </div>
            <div>
              <label>참조 ID</label>
              <input
                type="text"
                value={transferForm.referenceId}
                onChange={e => setTransferForm(prev => ({ ...prev, referenceId: e.target.value }))}
                placeholder="Optional"
              />
            </div>
          </div>
          <div className="account-row">
            <div>
              <label>출금 메모</label>
              <input
                type="text"
                value={transferForm.fromDescription}
                onChange={e => setTransferForm(prev => ({ ...prev, fromDescription: e.target.value }))}
                placeholder="메모"
              />
            </div>
            <div>
              <label>입금 메모</label>
              <input
                type="text"
                value={transferForm.toDescription}
                onChange={e => setTransferForm(prev => ({ ...prev, toDescription: e.target.value }))}
                placeholder="메모"
              />
            </div>
          </div>
          <button type="submit" className="primary-button">이체</button>
        </form>
      </section>
    </div>
  )
}
