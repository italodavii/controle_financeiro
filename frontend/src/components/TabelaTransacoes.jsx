import { formatarData, formatarMoeda } from '../utils/formato'

export default function TabelaTransacoes({ transacoes, onExcluir }) {
  return (
    <table className="tabela">
      <thead>
        <tr>
          <th>Data</th>
          <th>Descrição</th>
          <th>Categoria</th>
          <th>Tipo</th>
          <th className="tabela--direita">Valor</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        {transacoes.map((t) => (
          <tr key={t.id}>
            <td>{formatarData(t.data)}</td>
            <td>{t.descricao}</td>
            <td>{t.categoriaLabel}</td>
            <td>
              <span className={`etiqueta etiqueta--${t.tipo.toLowerCase()}`}>
                {t.tipo === 'ENTRADA' ? 'Entrada' : 'Saída'}
              </span>
            </td>
            <td className={`tabela--direita valor valor--${t.tipo.toLowerCase()}`}>
              {t.tipo === 'ENTRADA' ? '+' : '-'} {formatarMoeda(t.valor)}
            </td>
            <td className="tabela--direita">
              <button
                type="button"
                className="botao-excluir"
                onClick={() => onExcluir(t.id)}
                aria-label={`Excluir ${t.descricao}`}
              >
                Excluir
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
