import { useState } from 'react'
import { criar } from '../api/transacoes'

const CATEGORIAS = {
  ENTRADA: [
    { valor: 'SALARIO', rotulo: 'Salário' },
    { valor: 'FREELA', rotulo: 'Freela' },
    { valor: 'RENDIMENTO', rotulo: 'Rendimento' },
    { valor: 'OUTRAS_ENTRADAS', rotulo: 'Outras entradas' },
  ],
  SAIDA: [
    { valor: 'ALIMENTACAO', rotulo: 'Alimentação' },
    { valor: 'TRANSPORTE', rotulo: 'Transporte' },
    { valor: 'MORADIA', rotulo: 'Moradia' },
    { valor: 'SAUDE', rotulo: 'Saúde' },
    { valor: 'LAZER', rotulo: 'Lazer' },
    { valor: 'EDUCACAO', rotulo: 'Educação' },
    { valor: 'OUTRAS_SAIDAS', rotulo: 'Outras saídas' },
  ],
}

// A data ja comeca no mes que esta sendo visualizado, para o
// cadastro cair no mes da tela em vez de exigir digitacao.
function formularioVazio(mes) {
  return {
    descricao: '',
    valor: '',
    tipo: 'SAIDA',
    categoria: 'ALIMENTACAO',
    data: `${mes}-01`,
  }
}

export default function FormTransacao({ mes, onCriada }) {
  const [dados, setDados] = useState(() => formularioVazio(mes))
  const [salvando, setSalvando] = useState(false)
  const [erro, setErro] = useState(null)

  const categorias = CATEGORIAS[dados.tipo]

  function alterar(campo, valor) {
    setDados((atual) => ({ ...atual, [campo]: valor }))
  }

  // Ao trocar o tipo, a categoria anterior pode nao pertencer mais
  // a ele, entao ja selecionamos a primeira categoria valida.
  function alterarTipo(tipo) {
    setDados((atual) => ({
      ...atual,
      tipo,
      categoria: CATEGORIAS[tipo][0].valor,
    }))
  }

  async function enviar(e) {
    e.preventDefault()
    setSalvando(true)
    setErro(null)

    try {
      const criada = await criar({ ...dados, valor: Number(dados.valor) })
      setDados(formularioVazio(mes))
      onCriada(criada)
    } catch (problema) {
      setErro(problema.message)
    } finally {
      setSalvando(false)
    }
  }

  return (
    <form className="form" onSubmit={enviar}>
      <h2>Nova transação</h2>

      <div className="form__linha">
        <label>
          Descrição
          <input
            type="text"
            value={dados.descricao}
            onChange={(e) => alterar('descricao', e.target.value)}
            minLength={3}
            maxLength={120}
            required
          />
        </label>

        <label>
          Valor
          <input
            type="number"
            step="0.01"
            min="0.01"
            value={dados.valor}
            onChange={(e) => alterar('valor', e.target.value)}
            required
          />
        </label>
      </div>

      <div className="form__linha">
        <label>
          Tipo
          <select value={dados.tipo} onChange={(e) => alterarTipo(e.target.value)}>
            <option value="ENTRADA">Entrada</option>
            <option value="SAIDA">Saída</option>
          </select>
        </label>

        <label>
          Categoria
          <select
            value={dados.categoria}
            onChange={(e) => alterar('categoria', e.target.value)}
          >
            {categorias.map((c) => (
              <option key={c.valor} value={c.valor}>{c.rotulo}</option>
            ))}
          </select>
        </label>

        <label>
          Data
          <input
            type="date"
            value={dados.data}
            onChange={(e) => alterar('data', e.target.value)}
            required
          />
        </label>
      </div>

      {erro && <p className="form__erro">{erro}</p>}

      <button type="submit" disabled={salvando}>
        {salvando ? 'Salvando...' : 'Adicionar'}
      </button>
    </form>
  )
}
