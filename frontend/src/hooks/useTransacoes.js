import { useCallback, useEffect, useState } from 'react'
import { listar, obterResumo } from '../api/transacoes'

export function useTransacoes(mes) {
  const [transacoes, setTransacoes] = useState([])
  const [resumo, setResumo] = useState(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState(null)

  const buscar = useCallback(async () => {
    setCarregando(true)
    setErro(null)

    try {
      // As duas chamadas sao independentes, entao vao juntas.
      const [lista, totais] = await Promise.all([
        listar(mes),
        obterResumo(mes),
      ])
      setTransacoes(lista)
      setResumo(totais)
    } catch (e) {
      setErro(e.message)
      setTransacoes([])
      setResumo(null)
    } finally {
      setCarregando(false)
    }
  }, [mes])

  // Roda na primeira renderizacao e sempre que o mes mudar.
  useEffect(() => {
    buscar()
  }, [buscar])

  return { transacoes, resumo, carregando, erro, recarregar: buscar }
}
