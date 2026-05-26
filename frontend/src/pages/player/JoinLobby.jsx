import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { connectWS } from '../../api/ws'

export default function JoinLobby() {
  const [code, setCode] = useState('')
  const [nickname, setNick] = useState('')
  const [error, setError] = useState('')
  const nav = useNavigate()

  function join(e) {
    e.preventDefault()
    setError('')
    connectWS(client => {
      client.subscribe('/topic/lobby-error', msg => {
        setError(JSON.parse(msg.body).error)
      })

      client.publish({
        destination: '/app/lobby.join',
        body: JSON.stringify({ lobbyCode: code, nickname }),
      })

      // Subscribe to lobby updates — sessionId will come from LobbyEvent
      const sub = client.subscribe(`/topic/lobby.temp`, () => {})
      sub.unsubscribe()

      // Better: subscribe by broadcasting to all lobby.* and pick up the first event
      client.subscribe(`/topic/lobby.${code}`, msg => {
        // fallback — real session topic sub set in WaitingRoom
      })

      // Navigate optimistically, WaitingRoom will handle errors
      localStorage.setItem('playerNickname', nickname)
      localStorage.setItem('playerLobbyCode', code)
      nav('/player/waiting')
    })
  }

  return (
    <div style={card}>
      <h2>Dołącz do gry</h2>
      <form onSubmit={join} style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        <input placeholder="Kod (6 cyfr)" value={code} onChange={e => setCode(e.target.value)} maxLength={6} style={input} required />
        <input placeholder="Twój nick" value={nickname} onChange={e => setNick(e.target.value)} style={input} required />
        {error && <p style={{ color: 'red' }}>{error}</p>}
        <button type="submit" style={btn}>Dołącz</button>
      </form>
    </div>
  )
}

const card = { maxWidth: 380, margin: '80px auto', padding: 32, background: '#fff', borderRadius: 14, boxShadow: '0 2px 20px rgba(0,0,0,.15)' }
const input = { padding: '12px', fontSize: 18, borderRadius: 8, border: '1px solid #ccc', textAlign: 'center' }
const btn = { padding: '14px', background: '#e53935', color: '#fff', border: 'none', borderRadius: 10, fontSize: 18, cursor: 'pointer' }
