const moeda = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
})

export function formatarMoeda(valor) {
  return moeda.format(valor ?? 0)
}

/**
 * NAO usar new Date('2026-09-05'): o JS le essa string como UTC e,
 * no fuso do Brasil (UTC-3), ela volta como dia 04. Aqui a data e
 * so remontada como texto, sem passar por objeto Date.
 */
export function formatarData(iso) {
  if (!iso) return ''

  const [ano, mes, dia] = iso.split('-')
  return `${dia}/${mes}/${ano}`
}

export function mesAtual() {
  const agora = new Date()
  const ano = agora.getFullYear()
  const mes = String(agora.getMonth() + 1).padStart(2, '0')
  return `${ano}-${mes}`
}

export function formatarMes(mes) {
  if (!mes) return ''

  const [ano, numero] = mes.split('-')
  const nomes = [
    'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
    'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro',
  ]
  return `${nomes[Number(numero) - 1]} de ${ano}`
}
