package springpagina.web.financeiro_controle.dto;

import springpagina.web.financeiro_controle.domain.Categoria;

import java.math.BigDecimal;

public record TotalPorCategoria(
        Categoria categoria,
        String categoriaLabel,
        BigDecimal total
) {
}
