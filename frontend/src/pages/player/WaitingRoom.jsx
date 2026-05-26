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

    connectWS(client => {
      setConnected(true)

      // Join lobby
      client.publish({
        destination: '/app/lobby.join',
        body: JSON.stringify({ lobbyCode, nickname }),
      })

      // Wait for lobby events — we need sessionId from LobbyEvent
      // Backend broadcasts to /topic/lobby.{sessionId}
      // We subscribe to a temp channel, then resubscribe with real sessionId
      // Simpler approach: host shares session URL; for player we need sessionId.
      // Solution: backend returns sessionId via /queue/player join ack,
      // but since we don't have that, we use the HTTP fallback:
      // Actually let's subscribe to all lobby events for this code via a wildcard.
      // Since simple broker doesn't support wildcards, we use a search endpoint.

      // Pragmatic fix: GET /api/sessions?lobbyCode=XXXXXX to find sessionId
      fetch(`/api/sessions/by-code?code=${lobbyCode}`)
        .then(r => r.ok ? r.json() : null)
        .then(session => {
          if (!session) { setError('Nie znaleziono sesji o kodzie ' + lobbyCode); return }

          const sessionId = session.sessionId
          localStorage.setItem('playerSessionId', sessionId)

          subRef.current = client.subscribe(`/topic/lobby.${sessionId}`, msg => {
            const event = JSON.parse(msg.body)
            if (event.type === 'PLAYER_JOINED') {
              setPlayers(event.participants ?? [])
              if (event.playerId) localStorage.setItem('playerId', event.playerId)
            }
            if (event.type === 'GAME_STARTED') {
              nav('/player/game')
            }
          })
        })
        .catch(() => setError('Błąd połączenia z serwerem'))
    })

    return () => disconnectWS()
  }, [])

  if (error) return <div style={card}><p style={{ color: 'red' }}>{error}</p><button onClick={() => nav('/player')}>Wróć</button></div>

  return (
    <div style={card}>
      <h2>Lobby — {lobbyCode}</h2>
      <p>Zalogowany jako: <strong>{nickname}</strong></p>
      {!connected && <p>Łączenie...</p>}
      <h3>Gracze ({players.length}):</h3>
      <ul>{players.map(n => <li key={n} style={n === nickname ? { fontWeight: 'bold', color: '#1976d2' } : {}}>{n}</li>)}</ul>
      <p style={{ color: '#888', marginTop: 20 }}>Czekaj na hosta...</p>
    </div>
  )
}

const card = { maxWidth: 420, margin: '50px auto', padding: 32, background: '#fff', borderRadius: 14, boxShadow: '0 2px 20px rgba(0,0,0,.15)' }
