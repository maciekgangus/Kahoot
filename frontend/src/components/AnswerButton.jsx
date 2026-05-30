const COLORS = ['#e53935', '#1e88e5', '#43a047', '#fb8c00']

export default function AnswerButton({ answer, index, onClick, disabled, selected }) {
  return (
    <button
      onClick={() => onClick(answer)}
      disabled={disabled}
      style={{
        background: selected ? '#555' : COLORS[index % 4],
        color: '#fff',
        border: 'none',
        borderRadius: 12,
        padding: '20px 12px',
        fontSize: 17,
        cursor: disabled ? 'default' : 'pointer',
        opacity: disabled && !selected ? 0.5 : 1,
        fontWeight: 'bold',
        transition: 'opacity 0.2s',
        touchAction: 'manipulation',  // eliminates 300ms tap delay on mobile
        minHeight: 64,
        wordBreak: 'break-word',
        width: '100%',
      }}
    >
      {answer.content}
    </button>
  )
}
