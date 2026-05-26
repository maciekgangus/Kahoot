import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { connectWS, disconnectWS } from '../../api/ws'
import http from '../../api/http'
import Timer from '../../components/Timer'
import AnswerButton from '../../components/AnswerButton'
import RankingTable from '../../components/RankingTable'

export default function PlayerGame() {
  const [question, setQuestion] = useState(null)
  const [selected, setSelected] = useState(null)
  const [ranking, setRanking] = useState(null)
  const [phase, setPhase] = useState('WAITING') // WAITING | QUESTION | RANKING | FINISHED
  const clientRef = useRef(null)
  const questionStartRef = useRef(null)
  const nav = useNavigate()

  const sessionId = localStorage.getItem('playerSessionId')
  const playerId = localStorage.getItem('playerId')
  const nickname = localStorage.getItem('playerNickname')

  useEffect(() => {
    if (!sessionId) { nav('/player'); return }
    let cancelled = false

    connectWS(c => {
      if (cancelled) return  // StrictMode guard
      clientRef.current = c

      c.subscribe(`/topic/game.${sessionId}`, msg => {
        const event = JSON.parse(msg.body)
        if (event.questionId) {
          setQuestion(event)
          setSelected(null)
          setRanking(null)
          setPhase('QUESTION')
          questionStartRef.current = Date.now()
        } else if (event.ranking) {
          setRanking(event)
          setPhase(event.gameFinished ? 'FINISHED' : 'RANKING')
        }
      })

      // After subscribing, recover any question that was broadcast before we connected
      http.get(`/sessions/${sessionId}/current-question`)
        .then(r => {
          if (!cancelled && r.status === 200 && r.data?.questionId) {
            setQuestion(r.data)
            setSelected(null)
            setRanking(null)
            setPhase('QUESTION')
            questionStartRef.current = Date.now()
          }
        })
        .catch(() => {})  // 204 No Content or error → no active question, stay in WAITING
    })

    return () => { cancelled = true; disconnectWS() }
  }, [sessionId])

  function submitAnswer(answer) {
    if (selected || !clientRef.current) return
    setSelected(answer.id)
    const responseTimeMs = Date.now() - (questionStartRef.current ?? Date.now())

    clientRef.current.publish({
      destination: '/app/game.answer',
      body: JSON.stringify({
        sessionId,
        playerId,
        questionId: question.questionId,
        answerId: answer.id,
        responseTimeMs,
      }),
    })
  }

  return (
    <div style={{ maxWidth: 600, margin: '30px auto', padding: 24 }}>
      <p style={{ color: '#888' }}>{nickname}</p>

      {phase === 'WAITING' && <p>Czekaj na pytanie...</p>}

      {phase === 'QUESTION' && question && (
        <div>
          <p style={{ color: '#888' }}>Pytanie {question.questionNumber}/{question.totalQuestions}</p>
          <Timer seconds={question.timeLimitSec} />
          <h2 style={{ marginBottom: 20 }}>{question.content}</h2>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            {question.answers.map((a, i) => (
              <AnswerButton key={a.id} answer={a} index={i} onClick={submitAnswer}
                disabled={!!selected} selected={selected === a.id} />
            ))}
          </div>
          {selected && <p style={{ marginTop: 16, color: '#555' }}>Odpowiedź wysłana! Czekaj na wyniki...</p>}
        </div>
      )}

      {phase === 'RANKING' && ranking && (
        <div>
          <h3>Ranking po rundzie {ranking.currentQuestion}/{ranking.totalQuestions}</h3>
          <RankingTable ranking={ranking.ranking} />
          <p style={{ color: '#888', marginTop: 12 }}>Czekaj na następne pytanie...</p>
        </div>
      )}

      {phase === 'FINISHED' && ranking && (
        <div>
          <h2>🏆 Koniec!</h2>
          <RankingTable ranking={ranking.ranking} />
          <button type="button" onClick={() => nav('/player')} style={{ marginTop: 20, padding: '12px 24px', background: '#1976d2', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer' }}>
            Wróć do strony głównej
          </button>
        </div>
      )}
    </div>
  )
}
