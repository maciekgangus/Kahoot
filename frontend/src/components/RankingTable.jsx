export default function RankingTable({ ranking }) {
  if (!ranking?.length) return <p>Brak graczy.</p>
  return (
    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
      <thead>
        <tr style={{ background: '#1976d2', color: '#fff' }}>
          <th style={th}>#</th><th style={th}>Gracz</th><th style={th}>Punkty</th>
        </tr>
      </thead>
      <tbody>
        {ranking.map((p, i) => (
          <tr key={p.playerId} style={{ background: i % 2 === 0 ? '#f5f5f5' : '#fff' }}>
            <td style={td}>{p.rank}</td>
            <td style={td}>{p.nickname}</td>
            <td style={td}><strong>{p.totalScore}</strong></td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
const th = { padding: '8px 12px', textAlign: 'left' }
const td = { padding: '8px 12px' }
