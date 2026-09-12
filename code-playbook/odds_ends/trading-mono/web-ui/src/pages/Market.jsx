import React, { useState } from 'react'
import { getQuotesBulk, getSnapshot } from '../api/api'
import QuoteList from '../components/QuoteList'
import { useNavigate } from 'react-router-dom'

export default function Market() {
  const [symbols, setSymbols] = useState('AAPL,MSFT,GOOG')
  const [quotes, setQuotes] = useState({})
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const navigate = useNavigate()

  const onFetch = async () => {
    setLoading(true)
    setError(null)
    try {
      const resp = await getQuotesBulk(symbols)
      setQuotes(resp)
    } catch (e) {
      setError(e.response?.data || String(e))
    } finally {
      setLoading(false)
    }
  }

  const onSelect = async symbol => {
    // navigate to home with query param so Home loads this symbol
    navigate(`/?symbol=${encodeURIComponent(symbol)}`)
  }

  const onSnapshot = async symbol => {
    try {
      const snap = await getSnapshot(symbol)
      alert(JSON.stringify(snap, null, 2))
    } catch (e) {
      alert('Snapshot error: ' + (e.response?.data || e.message))
    }
  }

  return (
    <div className="container">
      <h2>Market</h2>
      <div className="form-row" style={{ marginBottom: 12 }}>
        <input type="text" value={symbols} onChange={e => setSymbols(e.target.value)} style={{ flex: 1 }} />
        <button onClick={onFetch} disabled={loading} style={{ marginLeft: 8 }}>{loading ? '로딩...' : '조회'}</button>
      </div>

      {error && <div style={{ color: 'red' }}>{JSON.stringify(error)}</div>}

      <QuoteList quotes={quotes} onSelect={onSelect} onSnapshot={onSnapshot} />
    </div>
  )
}
