import React, { useEffect, useState } from 'react'

const DEFAULT_SYMBOLS = ['AAPL','MSFT','GOOG','AMZN','TSLA','NVDA','META','NFLX','INTC','AMD']

export default function SymbolSearch({ value = '', onChange = () => {}, onSelect = () => {} }) {
  const [query, setQuery] = useState(value)
  const [open, setOpen] = useState(false)

  useEffect(() => setQuery(value || ''), [value])

  useEffect(() => {
    onChange(query)
  }, [query])

  const filtered = DEFAULT_SYMBOLS.filter(s => s.toLowerCase().includes((query || '').toLowerCase()))

  return (
    <div className="symbol-search">
      <input
        type="text"
        placeholder="심볼 입력 (예: AAPL)"
        value={query}
        onChange={e => { setQuery(e.target.value); setOpen(true) }}
        onFocus={() => setOpen(true)}
        onBlur={() => setTimeout(() => setOpen(false), 120)}
        className="symbol-input"
      />

      {open && filtered.length > 0 && (
        <ul className="autocomplete-list" role="listbox">
          {filtered.map(s => (
            <li key={s} role="option" onMouseDown={() => { setQuery(s); setOpen(false); onSelect(s) }}>{s}</li>
          ))}
        </ul>
      )}
    </div>
  )
}
