import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Home from './pages/Home'
import Register from './pages/host/Register'
import Login from './pages/host/Login'
import Dashboard from './pages/host/Dashboard'
import ManageQuiz from './pages/host/ManageQuiz'
import HostGame from './pages/host/HostGame'
import JoinLobby from './pages/player/JoinLobby'
import WaitingRoom from './pages/player/WaitingRoom'
import PlayerGame from './pages/player/PlayerGame'

export default function App() {
  return (
    <BrowserRouter>
      <div style={{ minHeight: '100vh', background: '#f0f4f8', fontFamily: 'sans-serif' }}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/host/register" element={<Register />} />
          <Route path="/host/login" element={<Login />} />
          <Route path="/host/dashboard" element={<Dashboard />} />
          <Route path="/host/quiz/:quizId" element={<ManageQuiz />} />
          <Route path="/host/game/:sessionId" element={<HostGame />} />
          <Route path="/player" element={<JoinLobby />} />
          <Route path="/player/waiting" element={<WaitingRoom />} />
          <Route path="/player/game" element={<PlayerGame />} />
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </div>
    </BrowserRouter>
  )
}
