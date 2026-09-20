const BASE = '/api'

/**
 * O fetch nao lanca erro quando o status e 4xx ou 5xx: ele so
 * rejeita se a rede falhar. Por isso toda resposta passa por aqui,
 * que le o corpo de erro do backend ({ mensagem, campos }) e
 * transforma em um Error de verdade, para a tela poder exibir.
 */
async function verificar(resposta) {
  if (resposta.ok) {
    return resposta
  }

  let mensagem = `Erro ${resposta.status} ao falar com o servidor.`

  try {
    const corpo = await resposta.json()
    if (corpo?.mensagem) {
      mensagem = corpo.mensagem
    }
    if (corpo?.campos) {
      const detalhes = Object.entries(corpo.campos)
        .map(([campo, erro]) => `${campo}: ${erro}`)
        .join(' | ')
      mensagem = `${mensagem} (${detalhes})`
    }
  } catch {
    // Resposta sem corpo JSON (ex.: 500 em HTML). Mantem a mensagem padrao.
  }

  throw new Error(mensagem)
}

export async function listar(mes, tipo, categoria) {
  const params = new URLSearchParams()
  if (mes) params.set('mes', mes)
  if (tipo) params.set('tipo', tipo)
  if (categoria) params.set('categoria', categoria)

  const resposta = await fetch(`${BASE}/transacoes?${params}`)
  await verificar(resposta)
  return resposta.json()
}

export async function obterResumo(mes) {
  const params = new URLSearchParams()
  if (mes) params.set('mes', mes)

  const resposta = await fetch(`${BASE}/resumo?${params}`)
  await verificar(resposta)
  return resposta.json()
}

export async function criar(dados) {
  const resposta = await fetch(`${BASE}/transacoes`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(dados),
  })
  await verificar(resposta)
  return resposta.json()
}

export async function excluir(id) {
  const resposta = await fetch(`${BASE}/transacoes/${id}`, {
    method: 'DELETE',
  })
  await verificar(resposta)
  // 204 nao tem corpo, entao nao ha nada para devolver.
}
