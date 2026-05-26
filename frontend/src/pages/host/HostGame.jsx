import { useEffect, useRef, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { connectWS, disconnectWS } from '../../api/ws'
import http from '../../api/http'
import RankingTable from '../../components/RankingTable'

export default function HostGame() {
  const { sessionId } = useParams()
  const [session, setSession] = useState(null)
  const [players, setPlayers] = useState([])
  const [question, setQuestion] = useState(null)
  const [ranking, setRanking] = useState(null)
  const [phase, setPhase] = useState('LOBBY') // LOBBY | PLAYING | RANKING | FINISHED
  const [started, setStarted] = useState(false)
  const clientRef = useRef(null)
  const nav = useNavigate()
  const hostId = localStorage.getItem('hostId')

  useEffect(() => {
    let cancelled = false

    http.get('/sessions/' + sessionId).then(r => {
      if (!cancelled) {
        setSession(r.data)
        // Seed participants from HTTP so the Start button is enabled even if PLAYER_JOINED event was missed
        if (r.data.participants?.length) setPlayers(r.data.participants)
      }
    })

    connectWS(c => {
      if (cancelled) return  // StrictMode guard — only the second effect mount survives
      clientRef.current = c

      c.subscribe(`/topic/lobby.${sessionId}`, msg => {
        const event = JSON.parse(msg.body)
        if (event.type === 'PLAYER_JOINED') setPlayers(event.participants ?? [])
        if (event.type === 'GAME_STARTED') { setPhase('PLAYING'); setStarted(true) }
      })

      c.subscribe(`/topic/game.${sessionId}`, msg => {
        const event = JSON.parse(msg.body)
        if (event.questionId) {
          setQuestion(event); setRanking(null); setPhase('PLAYING')
        } else if (event.ranking) {
          setRanking(event)
          setPhase(event.gameFinished ? 'FINISHED' : 'RANKING')
        }
      })
    })

    return () => { cancelled = true; disconnectWS() }
  }, [sessionId])

  async function startGame() {
    await http.post(`/sessions/${sessionId}/start`, null, { params: { hostId } })
  }

  function nextQuestion() {
    clientRef.current?.publish({
      destination: '/app/game.nextQuestion',
      body: JSON.stringify({ sessionId, hostId: Number(hostId) }),
    })
  }

  if (!session) return <p>Ładowanie...</p>

  return (
    <div style={{ maxWidth: 760, margin: '30px auto', padding: 24 }}>
      <h2>🎮 Gra jako Host</h2>
      <p><strong>Kod lobby:</strong> <span style={{ fontSize: 28, letterSpacing: 6, fontFamily: 'monospace', color: '#1976d2' }}>{session.lobbyCode}</span></p>

      {phase === 'LOBBY' && (
        <div>
          <h3>Gracze w lobby ({players.length}):</h3>
          <ul>{players.map(n => <li key={n}>{n}</li>)}</ul>
          <button onClick={startGame} style={btn} disabled={players.length === 0}>▶ Start!</button>
        </div>
      )}

      {phase === 'PLAYING' && question && (
        <div>
          <h3>Pytanie {question.questionNumber}/{question.totalQuestions}</h3>
          <p style={{ fontSize: 22 }}>{question.content}</p>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
            {question.answers.map(a => (
              <div key={a.id} style={{ background: '#e3f2fd', padding: 14, borderRadius: 8 }}>{a.content}</div>
            ))}
          </div>
          <p style={{ color: '#888' }}>Oczekiwanie na odpowiedzi... (timeout po {question.timeLimitSec}s)</p>
        </div>
      )}

      {phase === 'RANKING' && ranking && (
        <div>
          <h3>Ranking po rundzie {ranking.currentQuestion}/{ranking.totalQuestions}</h3>
          <RankingTable ranking={ranking.ranking} />
          <button onClick={nextQuestion} style={{ ...btn, marginTop: 16 }}>➡ Następne pytanie</button>
        </div>
      )}

      {phase === 'FINISHED' && ranking && (
        <div>
          <h2>🏆 Koniec gry!</h2>
          <RankingTable ranking={ranking.ranking} />
          <button onClick={() => nav('/host/dashboard')} style={{ ...btn, marginTop: 16 }}>Powrót do panelu</button>
        </div>
      )}
    </div>
  )
}

const btn = { padding: '14px 24px', background: '#1976d2', color: '#fff', border: 'none', borderRadius: 10, fontSize: 17, cursor: 'pointer' }
