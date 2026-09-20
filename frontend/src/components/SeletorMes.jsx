import { formatarMes } from '../utils/formato'

export default function SeletorMes({ mes, onChange }) {
  return (
    <div className="seletor-mes">
      <label htmlFor="mes">Mês de referência</label>
      <input
        id="mes"
        type="month"
        value={mes}
        onChange={(e) => onChange(e.target.value)}
      />
      <span className="seletor-mes__rotulo">{formatarMes(mes)}</span>
    </div>
  )
}
