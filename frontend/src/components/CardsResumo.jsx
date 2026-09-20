import { formatarMoeda } from '../utils/formato'

export default function CardsResumo({ resumo }) {
  if (!resumo) return null

  const saldoPositivo = Number(resumo.saldo) >= 0

  return (
    <section className="cards">
      <article className="card card--entrada">
        <h2>Entradas</h2>
        <p>{formatarMoeda(resumo.totalEntradas)}</p>
      </article>

      <article className="card card--saida">
        <h2>Saídas</h2>
        <p>{formatarMoeda(resumo.totalSaidas)}</p>
      </article>

      <article className={`card card--saldo ${saldoPositivo ? 'card--entrada' : 'card--saida'}`}>
        <h2>Saldo</h2>
        <p>{formatarMoeda(resumo.saldo)}</p>
      </article>
    </section>
  )
}
