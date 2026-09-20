package springpagina.web.financeiro_controle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import springpagina.web.financeiro_controle.domain.Categoria;
import springpagina.web.financeiro_controle.domain.TipoTransacao;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoRequest(

        @NotBlank
        @Size(min = 3, max = 120)
        String descricao,

        @NotNull
        @Positive
        BigDecimal valor,

        @NotNull
        TipoTransacao tipo,

        @NotNull
        Categoria categoria,

        @NotNull
        LocalDate data
) {
}
