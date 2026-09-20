package springpagina.web.financeiro_controle.dto;

import springpagina.web.financeiro_controle.domain.Categoria;
import springpagina.web.financeiro_controle.domain.TipoTransacao;
import springpagina.web.financeiro_controle.domain.Transacao;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        TipoTransacao tipo,
        Categoria categoria,
        String categoriaLabel,
        LocalDate data
) {

    public static TransacaoResponse from(Transacao transacao) {
        return new TransacaoResponse(
                transacao.getId(),
                transacao.getDescricao(),
                transacao.getValor(),
                transacao.getTipo(),
                transacao.getCategoria(),
                transacao.getCategoria().getRotulo(),
                transacao.getData()
        );
    }
}
