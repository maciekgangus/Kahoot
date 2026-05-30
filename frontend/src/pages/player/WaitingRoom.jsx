import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { connectWS, disconnectWS } from '../../api/ws'

export default function WaitingRoom() {
  const [players, setPlayers] = useState([])
  const [error, setError] = useState('')
  const [connected, setConnected] = useState(false)
  const nav = useNavigate()
  const subRef = useRef(null)

  const nickname = localStorage.getItem('playerNickname')
  const lobbyCode = localStorage.getItem('playerLobbyCode')

  useEffect(() => {
    if (!nickname || !lobbyCode) { nav('/player'); return }
    let cancelled = false  // guards against StrictMode double-invoke

    // 1. Get sessionId via HTTP first
    fetch(`/api/sessions/by-code?code=${lobbyCode}`)
      .then(r => r.ok ? r.json() : Promise.reject('not found'))
      .then(session => {
        if (cancelled) return  // effect was cleaned up — skip

        const sessionId = session.sessionId
        localStorage.setItem('playerSessionId', sessionId)

        // 2. Connect WS → subscribe FIRST → then publish join
        connectWS(c => {
          if (cancelled) return  // guard in case cleanup ran during connection
          setConnected(true)

          // Subscribe before publishing so we don't miss our own PLAYER_JOINED event
          subRef.current = c.subscribe(`/topic/lobby.${sessionId}`, msg => {
            const event = JSON.parse(msg.body)
            if (event.type === 'PLAYER_JOINED') {
              setPlayers(event.participants ?? [])
              if (event.playerId) localStorage.setItem('playerId', event.playerId)
            }
            if (event.type === 'GAME_STARTED') {
              nav('/player/game')
            }
          })

          // Now send join — subscription is already active
          c.publish({
            destination: '/app/lobby.join',
            body: JSON.stringify({ lobbyCode, nickname }),
          })
        })
      })
      .catch(() => { if (!cancelled) setError('Nie znaleziono sesji o kodzie ' + lobbyCode) })

    return () => {
      cancelled = true
      disconnectWS()
    }
  }, [])

  if (error) return (
    <div style={card}>
      <p style={{ color: 'red' }}>{error}</p>
      <button type="button" onClick={() => nav('/player')} style={{ marginTop: 10, cursor: 'pointer' }}>Wróć</button>
    </div>
  )

  return (
    <div style={card}>
      <h2>Lobby — {lobbyCode}</h2>
      <p>Zalogowany jako: <strong>{nickname}</strong></p>
      {!connected && <p style={{ color: '#888' }}>Łączenie...</p>}
      <h3>Gracze ({players.length}):</h3>
      <ul>
        {players.map((n, i) => (
          <li key={`${n}-${i}`} style={n === nickname ? { fontWeight: 'bold', color: '#1976d2' } : {}}>{n}</li>
        ))}
      </ul>
      <p style={{ color: '#888', marginTop: 20 }}>Czekaj na hosta...</p>
    </div>
  )
}

const card = { maxWidth: 420, margin: '20px auto', padding: '24px 20px', background: '#fff', borderRadius: 14, boxShadow: '0 2px 20px rgba(0,0,0,.15)' }
