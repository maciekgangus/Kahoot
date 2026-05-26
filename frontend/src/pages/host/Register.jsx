import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import http from '../../api/http'

export default function Register() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const nav = useNavigate()

  async function submit() {
    setError('')
    try {
      const res = await http.post('/hosts/register', { username, password })
      localStorage.setItem('hostId', res.data.id)
      localStorage.setItem('hostUsername', res.data.username)
      nav('/host/dashboard')
    } catch (err) {
      setError(err.response?.data?.error || err.response?.data?.username || 'Błąd rejestracji')
    }
  }

  return (
    <div style={card}>
      <h2>Zarejestruj się jako Host</h2>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        <input placeholder="Login" value={username} onChange={e => setUsername(e.target.value)} style={input} />
        <input placeholder="Hasło" type="password" value={password} onChange={e => setPassword(e.target.value)} style={input} />
        {error && <p style={{ color: 'red' }}>{error}</p>}
        <button type="button" onClick={submit} style={btn}>Zarejestruj</button>
        <button type="button" style={{ ...btn, background: '#888' }} onClick={() => nav('/')}>Wróć</button>
      </div>
    </div>
  )
}

const card = { maxWidth: 400, margin: '60px auto', padding: 32, background: '#fff', borderRadius: 12, boxShadow: '0 2px 16px rgba(0,0,0,.15)' }
const input = { padding: '10px 12px', fontSize: 16, borderRadius: 6, border: '1px solid #ccc' }
const btn = { padding: '12px', background: '#1976d2', color: '#fff', border: 'none', borderRadius: 8, fontSize: 16, cursor: 'pointer' }
