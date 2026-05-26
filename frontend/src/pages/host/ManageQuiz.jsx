import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import http from '../../api/http'

export default function ManageQuiz() {
  const { quizId } = useParams()
  const [quiz, setQuiz] = useState(null)
  const [qContent, setQContent] = useState('')
  const [qTime, setQTime] = useState('')
  const nav = useNavigate()

  async function load() {
    const r = await http.get('/quizzes/' + quizId)
    setQuiz(r.data)
  }

  useEffect(() => { load() }, [])

  async function addQuestion() {
    if (!qContent.trim()) return
    await http.post(`/quizzes/${quizId}/questions`, { content: qContent, timeLimitSec: qTime ? +qTime : undefined })
    setQContent(''); setQTime(''); load()
  }

  async function addAnswer(questionId, content, isCorrect) {
    await http.post(`/quizzes/${quizId}/questions/${questionId}/answers`, { content, isCorrect })
    load()
  }

  async function startSession() {
    const hostId = localStorage.getItem('hostId')
    const r = await http.post('/sessions', null, { params: { quizId, hostId } })
    nav('/host/game/' + r.data.sessionId)
  }

  if (!quiz) return <p>Ładowanie...</p>

  return (
    <div style={{ maxWidth: 760, margin: '30px auto', padding: 24 }}>
      <h2>{quiz.title}</h2>
      <button onClick={() => nav('/host/dashboard')} style={backBtn}>← Wróć</button>
      <button onClick={startSession} style={startBtn}>▶ Uruchom Sesję</button>

      <div style={card}>
        <h3>Dodaj pytanie</h3>
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
          <input placeholder="Treść pytania" value={qContent} onChange={e => setQContent(e.target.value)} style={{ ...input, flex: 1 }} />
          <input placeholder="Czas (s)" type="number" value={qTime} onChange={e => setQTime(e.target.value)} style={{ ...input, width: 80 }} />
          <button type="button" onClick={addQuestion} style={btn}>Dodaj</button>
        </div>
      </div>

      {quiz.questions.map((q, qi) => (
        <QuestionCard key={q.id} q={q} qi={qi} quizId={quizId} onAddAnswer={addAnswer} onReload={load} />
      ))}
    </div>
  )
}

function QuestionCard({ q, qi, quizId, onAddAnswer, onReload }) {
  const [ans, setAns] = useState('')
  const [correct, setCorrect] = useState(false)

  return (
    <div style={{ ...card, marginBottom: 14 }}>
      <strong>P{qi + 1}: {q.content}</strong> {q.timeLimitSec && <span style={{ color: '#888' }}>({q.timeLimitSec}s)</span>}
      <ul style={{ margin: '8px 0' }}>
        {q.answers.map(a => (
          <li key={a.id} style={{ color: a.isCorrect ? '#2e7d32' : '#333' }}>
            {a.isCorrect ? '✓ ' : '✗ '}{a.content}
          </li>
        ))}
      </ul>
      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'center' }}>
        <input placeholder="Odpowiedź" value={ans} onChange={e => setAns(e.target.value)} style={{ ...input, flex: 1 }} />
        <label><input type="checkbox" checked={correct} onChange={e => setCorrect(e.target.checked)} /> Poprawna</label>
        <button onClick={() => { onAddAnswer(q.id, ans, correct); setAns(''); setCorrect(false) }} style={btn} disabled={!ans}>Dodaj odpowiedź</button>
      </div>
    </div>
  )
}

const card = { background: '#fff', borderRadius: 10, boxShadow: '0 1px 8px rgba(0,0,0,.1)', padding: 20 }
const input = { padding: '10px 12px', fontSize: 15, borderRadius: 6, border: '1px solid #ccc' }
const btn = { padding: '10px 16px', background: '#1976d2', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer' }
const backBtn = { ...btn, background: '#888', marginRight: 8, marginBottom: 16 }
const startBtn = { ...btn, background: '#2e7d32', marginBottom: 16 }
