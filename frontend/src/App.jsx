import { useState } from 'react'
import './App.css'
import { excluir } from './api/transacoes'
import CardsResumo from './components/CardsResumo'
import EstadoVazio from './components/EstadoVazio'
import FormTransacao from './components/FormTransacao'
import SeletorMes from './components/SeletorMes'
import TabelaTransacoes from './components/TabelaTransacoes'
import { useTransacoes } from './hooks/useTransacoes'
import { mesAtual } from './utils/formato'

export default function App() {
  const [mes, setMes] = useState(mesAtual())
  const { transacoes, resumo, carregando, erro, recarregar } = useTransacoes(mes)
  const [erroExclusao, setErroExclusao] = useState(null)

  // Toda troca de mes passa por aqui, para o erro de exclusao do mes
  // anterior nao ficar pendurado na tela do mes novo.
  function trocarMes(novoMes) {
    setMes(novoMes)
    setErroExclusao(null)
  }

  // A transacao pode ter sido cadastrada com data de outro mes. Nesse
  // caso, em vez de ela sumir da tela, navegamos para o mes dela.
  function aoCriar(transacao) {
    const mesDaTransacao = transacao.data.slice(0, 7)

    if (mesDaTransacao !== mes) {
      trocarMes(mesDaTransacao)
    } else {
      recarregar()
    }
  }

  async function removerTransacao(id) {
    setErroExclusao(null)
    try {
      await excluir(id)
      recarregar()
    } catch (problema) {
      setErroExclusao(problema.message)
    }
  }

  return (
    <div className="pagina">
      <header className="cabecalho">
        <h1>Controle Financeiro</h1>
      </header>

      <SeletorMes mes={mes} onChange={trocarMes} />

      {carregando && (
        <div className="estado estado--carregando">
          <p>Carregando...</p>
        </div>
      )}

      {!carregando && erro && (
        <div className="estado estado--erro">
          <h2>Não foi possível carregar os dados</h2>
          <p>{erro}</p>
          <p className="estado--dica">
            Verifique se o backend está rodando em localhost:8080.
          </p>
          <button type="button" onClick={recarregar}>Tentar novamente</button>
        </div>
      )}

      {!carregando && !erro && (
        <>
          <CardsResumo resumo={resumo} />

          <FormTransacao mes={mes} onCriada={aoCriar} />

          {erroExclusao && <p className="form__erro">{erroExclusao}</p>}

          {transacoes.length === 0
            ? <EstadoVazio />
            : <TabelaTransacoes transacoes={transacoes} onExcluir={removerTransacao} />}
        </>
      )}
    </div>
  )
}
