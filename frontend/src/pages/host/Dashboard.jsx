import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import http from '../../api/http'

export default function Dashboard() {
  const [quizzes, setQuizzes] = useState([])
  const [title, setTitle] = useState('')
  const [desc, setDesc] = useState('')
  const [timeLimit, setTimeLimit] = useState(20)
  const nav = useNavigate()
  const hostId = localStorage.getItem('hostId')

  useEffect(() => {
    if (!hostId) { nav('/'); return }
    http.get('/quizzes', { params: { hostId } }).then(r => setQuizzes(r.data))
  }, [])

  async function createQuiz(e) {
    e.preventDefault()
    const r = await http.post('/quizzes', { title, description: desc, defaultTimeLimitSec: timeLimit }, { params: { hostId } })
    nav('/host/quiz/' + r.data.id)
  }

  return (
    <div style={{ maxWidth: 700, margin: '40px auto', padding: 24 }}>
      <h2>Panel Hosta — {localStorage.getItem('hostUsername')}</h2>

      <div style={card}>
        <h3>Nowy Quiz</h3>
        <form onSubmit={createQuiz} style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          <input placeholder="Tytuł" value={title} onChange={e => setTitle(e.target.value)} style={input} required />
          <input placeholder="Opis" value={desc} onChange={e => setDesc(e.target.value)} style={input} />
          <label>Domyślny czas (s): <input type="number" min={5} max={120} value={timeLimit} onChange={e => setTimeLimit(+e.target.value)} style={{ ...input, width: 70 }} /></label>
          <button type="submit" style={btn}>Utwórz Quiz</button>
        </form>
      </div>

      <h3>Twoje Quizy</h3>
      {quizzes.length === 0 && <p>Brak quizów.</p>}
      {quizzes.map(q => (
        <div key={q.id} style={{ ...card, marginBottom: 10, cursor: 'pointer' }} onClick={() => nav('/host/quiz/' + q.id)}>
          <strong>{q.title}</strong> — {q.questions?.length ?? 0} pytań
        </div>
      ))}
    </div>
  )
}

const card = { background: '#fff', borderRadius: 10, boxShadow: '0 1px 8px rgba(0,0,0,.1)', padding: 20 }
const input = { padding: '10px 12px', fontSize: 16, borderRadius: 6, border: '1px solid #ccc' }
const btn = { padding: '12px', background: '#1976d2', color: '#fff', border: 'none', borderRadius: 8, fontSize: 16, cursor: 'pointer' }
