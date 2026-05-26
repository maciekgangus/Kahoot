import { useEffect, useState } from 'react'

export default function Timer({ seconds, onExpire }) {
  const [left, setLeft] = useState(seconds)

  useEffect(() => {
    setLeft(seconds)
    const id = setInterval(() => {
      setLeft(prev => {
        if (prev <= 1) { clearInterval(id); onExpire?.(); return 0 }
        return prev - 1
      })
    }, 1000)
    return () => clearInterval(id)
  }, [seconds])

  const pct = (left / seconds) * 100
  const color = left > seconds * 0.5 ? '#4caf50' : left > seconds * 0.25 ? '#ff9800' : '#f44336'

  return (
    <div style={{ textAlign: 'center', marginBottom: 12 }}>
      <div style={{ fontSize: 48, fontWeight: 'bold', color }}>{left}</div>
      <div style={{ background: '#eee', borderRadius: 8, height: 10, overflow: 'hidden' }}>
        <div style={{ width: `${pct}%`, background: color, height: '100%', transition: 'width 1s linear' }} />
      </div>
    </div>
  )
}
