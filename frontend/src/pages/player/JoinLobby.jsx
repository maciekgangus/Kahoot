import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

export default function JoinLobby() {
  const [code, setCode] = useState('')
  const [nickname, setNick] = useState('')
  const [error, setError] = useState('')
  const nav = useNavigate()

  function join() {
    setError('')
    if (!code.trim() || code.length !== 6) { setError('Podaj 6-cyfrowy kod'); return }
    if (!nickname.trim()) { setError('Podaj nick'); return }
    localStorage.setItem('playerNickname', nickname.trim())
    localStorage.setItem('playerLobbyCode', code.trim())
    nav('/player/waiting')
  }

  return (
    <div style={card}>
      <h2>Dołącz do gry</h2>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        <input placeholder="Kod (6 cyfr)" value={code} onChange={e => setCode(e.target.value)} maxLength={6} style={input} />
        <input placeholder="Twój nick" value={nickname} onChange={e => setNick(e.target.value)} style={input} />
        {error && <p style={{ color: 'red' }}>{error}</p>}
        <button type="button" onClick={join} style={btn}>Dołącz</button>
      </div>
    </div>
  )
}

const card = { maxWidth: 380, margin: '80px auto', padding: 32, background: '#fff', borderRadius: 14, boxShadow: '0 2px 20px rgba(0,0,0,.15)' }
const input = { padding: '12px', fontSize: 18, borderRadius: 8, border: '1px solid #ccc', textAlign: 'center' }
const btn = { padding: '14px', background: '#e53935', color: '#fff', border: 'none', borderRadius: 10, fontSize: 18, cursor: 'pointer' }
