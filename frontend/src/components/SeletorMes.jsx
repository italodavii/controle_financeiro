import { formatarMes } from '../utils/formato'

export default function SeletorMes({ mes, onChange }) {
  return (
    <div className="seletor-mes">
      <label htmlFor="mes">Mês de referência</label>
      <input
        id="mes"
        type="month"
        value={mes}
        onChange={(e) => {
          // Limpar o campo dispara onChange com string vazia. Ignoramos,
          // senao a tela ficaria sem mes enquanto os dados seguem sendo
          // os do mes anterior.
          if (e.target.value) {
            onChange(e.target.value)
          }
        }}
      />
      <span className="seletor-mes__rotulo">{formatarMes(mes)}</span>
    </div>
  )
}
