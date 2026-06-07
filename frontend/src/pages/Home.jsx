import { useNavigate } from 'react-router-dom'

export default function Home() {
  const nav = useNavigate()
  return (
    <div style={{ textAlign: 'center', padding: '80px 20px', fontFamily: 'sans-serif' }}>
      <h1 style={{ fontSize: 42, marginBottom: 8 }}>🎯 QuizRT</h1>
      <p style={{ color: '#666', marginBottom: 40 }}>Platforma interaktywnych quizów czasu rzeczywistego</p>
      <div style={{ display: 'flex', gap: 20, justifyContent: 'center', flexWrap: 'wrap' }}>
        <div style={card}>
          <h2>🎤 Jestem Hostem</h2>
          <p>Twórz quizy i prowadź sesje</p>
          <button style={btnHost} onClick={() => nav('/host/register')}>Zarejestruj się</button>
          <br /><br />
          <button style={{ ...btnHost, background: '#888' }} onClick={() => nav('/host/login')}>
            Mam już konto → Zaloguj
          </button>
        </div>
        <div style={card}>
          <h2>🎮 Jestem Graczem</h2>
          <p>Dołącz do gry kodem</p>
          <button style={btnPlayer} onClick={() => nav('/player')}>Dołącz</button>
        </div>
      </div>
    </div>
  )
}

const card = { background: '#fff', borderRadius: 16, padding: '32px 40px', boxShadow: '0 4px 24px rgba(0,0,0,.12)', minWidth: 260 }
const btnHost = { padding: '14px 28px', background: '#1976d2', color: '#fff', border: 'none', borderRadius: 10, fontSize: 16, cursor: 'pointer' }
const btnPlayer = { padding: '14px 28px', background: '#e53935', color: '#fff', border: 'none', borderRadius: 10, fontSize: 16, cursor: 'pointer' }
