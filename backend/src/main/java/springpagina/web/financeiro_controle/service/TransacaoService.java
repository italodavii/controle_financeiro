package springpagina.web.financeiro_controle.service;

import org.springframework.stereotype.Service;
import springpagina.web.financeiro_controle.domain.Categoria;
import springpagina.web.financeiro_controle.domain.TipoTransacao;
import springpagina.web.financeiro_controle.domain.Transacao;
import springpagina.web.financeiro_controle.dto.ResumoResponse;
import springpagina.web.financeiro_controle.dto.TotalPorCategoria;
import springpagina.web.financeiro_controle.dto.TransacaoRequest;
import springpagina.web.financeiro_controle.dto.TransacaoResponse;
import springpagina.web.financeiro_controle.exception.RegraNegocioException;
import springpagina.web.financeiro_controle.exception.TransacaoNaoEncontradaException;
import springpagina.web.financeiro_controle.repository.TransacaoRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransacaoService {

    private final TransacaoRepository repository;

    public TransacaoService(TransacaoRepository repository) {
        this.repository = repository;
    }

    public List<TransacaoResponse> listar(YearMonth mes, TipoTransacao tipo, Categoria categoria) {
        List<Transacao> transacoes = buscarDoMes(mes);

        List<TransacaoResponse> resultado = new ArrayList<>();
        for (Transacao transacao : transacoes) {
            if (tipo != null && transacao.getTipo() != tipo) {
                continue;
            }
            if (categoria != null && transacao.getCategoria() != categoria) {
                continue;
            }
            resultado.add(TransacaoResponse.from(transacao));
        }
        return resultado;
    }

    public TransacaoResponse buscarPorId(Long id) {
        Transacao transacao = repository.findById(id)
                .orElseThrow(() -> new TransacaoNaoEncontradaException(id));
        return TransacaoResponse.from(transacao);
    }

    public TransacaoResponse criar(TransacaoRequest request) {
        validarCategoriaCombinaComTipo(request.tipo(), request.categoria());

        Transacao transacao = new Transacao();
        copiarDados(request, transacao);

        return TransacaoResponse.from(repository.save(transacao));
    }

    public TransacaoResponse atualizar(Long id, TransacaoRequest request) {
        validarCategoriaCombinaComTipo(request.tipo(), request.categoria());

        Transacao transacao = repository.findById(id)
                .orElseThrow(() -> new TransacaoNaoEncontradaException(id));
        copiarDados(request, transacao);

        return TransacaoResponse.from(repository.save(transacao));
    }

    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new TransacaoNaoEncontradaException(id);
        }
        repository.deleteById(id);
    }

    public ResumoResponse calcularResumo(YearMonth mes) {
        YearMonth mesUsado = (mes == null) ? YearMonth.now() : mes;
        List<Transacao> transacoes = buscarDoMes(mesUsado);

        BigDecimal totalEntradas = BigDecimal.ZERO;
        BigDecimal totalSaidas = BigDecimal.ZERO;
        Map<Categoria, BigDecimal> totaisPorCategoria = new LinkedHashMap<>();

        for (Transacao transacao : transacoes) {
            BigDecimal valor = transacao.getValor();

            if (transacao.getTipo() == TipoTransacao.ENTRADA) {
                totalEntradas = totalEntradas.add(valor);
            } else {
                totalSaidas = totalSaidas.add(valor);
            }

            totaisPorCategoria.merge(transacao.getCategoria(), valor, BigDecimal::add);
        }

        BigDecimal saldo = totalEntradas.subtract(totalSaidas);

        List<TotalPorCategoria> porCategoria = new ArrayList<>();
        for (Map.Entry<Categoria, BigDecimal> entrada : totaisPorCategoria.entrySet()) {
            Categoria categoria = entrada.getKey();
            porCategoria.add(new TotalPorCategoria(categoria, categoria.getRotulo(), entrada.getValue()));
        }

        return new ResumoResponse(mesUsado, totalEntradas, totalSaidas, saldo, porCategoria);
    }

    private List<Transacao> buscarDoMes(YearMonth mes) {
        YearMonth mesUsado = (mes == null) ? YearMonth.now() : mes;
        LocalDate inicio = mesUsado.atDay(1);
        LocalDate fim = mesUsado.atEndOfMonth();
        return repository.findByDataBetweenOrderByDataDescIdDesc(inicio, fim);
    }

    private void copiarDados(TransacaoRequest request, Transacao transacao) {
        transacao.setDescricao(request.descricao());
        transacao.setValor(request.valor());
        transacao.setTipo(request.tipo());
        transacao.setCategoria(request.categoria());
        transacao.setData(request.data());
    }

    private void validarCategoriaCombinaComTipo(TipoTransacao tipo, Categoria categoria) {
        if (categoria.getTipo() != tipo) {
            throw new RegraNegocioException(
                    "A categoria " + categoria.name()
                            + " pertence a " + categoria.getTipo()
                            + " e nao pode ser usada em uma transacao de " + tipo);
        }
    }
}
