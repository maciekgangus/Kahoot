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
        borderRadius: 8,
        padding: '18px 12px',
        fontSize: 16,
        cursor: disabled ? 'default' : 'pointer',
        opacity: disabled && !selected ? 0.5 : 1,
        fontWeight: selected ? 'bold' : 'normal',
        transition: 'opacity 0.2s',
      }}
    >
      {answer.content}
    </button>
  )
}
