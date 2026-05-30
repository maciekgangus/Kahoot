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
    <div style={{ maxWidth: 600, margin: '0 auto', padding: '16px 16px env(safe-area-inset-bottom, 16px)' }}>
      <p style={{ color: '#888', margin: '8px 0 4px', fontSize: 14 }}>{nickname}</p>

      {phase === 'WAITING' && <p style={{ marginTop: 40, textAlign: 'center', color: '#555' }}>Czekaj na pytanie...</p>}

      {phase === 'QUESTION' && question && (
        <div>
          <p style={{ color: '#888', margin: '0 0 4px', fontSize: 13 }}>Pytanie {question.questionNumber}/{question.totalQuestions}</p>
          <Timer seconds={question.timeLimitSec} />
          <h2 style={{ margin: '12px 0 16px', fontSize: 'clamp(18px, 5vw, 26px)', lineHeight: 1.3 }}>{question.content}</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 10 }}>
            {question.answers.map((a, i) => (
              <AnswerButton key={a.id} answer={a} index={i} onClick={submitAnswer}
                disabled={!!selected} selected={selected === a.id} />
            ))}
          </div>
          {selected && <p style={{ marginTop: 16, color: '#555', textAlign: 'center' }}>Odpowiedź wysłana! Czekaj na wyniki...</p>}
        </div>
      )}

      {phase === 'RANKING' && ranking && (
        <div>
          <h3 style={{ margin: '12px 0' }}>Ranking po rundzie {ranking.currentQuestion}/{ranking.totalQuestions}</h3>
          <RankingTable ranking={ranking.ranking} />
          <p style={{ color: '#888', marginTop: 12, textAlign: 'center' }}>Czekaj na następne pytanie...</p>
        </div>
      )}

      {phase === 'FINISHED' && ranking && (
        <div>
          <h2 style={{ textAlign: 'center' }}>🏆 Koniec!</h2>
          <RankingTable ranking={ranking.ranking} />
          <button type="button" onClick={() => nav('/player')} style={backBtn}>
            Wróć do strony głównej
          </button>
        </div>
      )}
    </div>
  )
}

const backBtn = { marginTop: 20, padding: '14px', width: '100%', background: '#1976d2', color: '#fff', border: 'none', borderRadius: 10, fontSize: 16, cursor: 'pointer', touchAction: 'manipulation' }
