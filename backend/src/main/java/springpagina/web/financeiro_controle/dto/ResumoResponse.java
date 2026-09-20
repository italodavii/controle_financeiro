package springpagina.web.financeiro_controle.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record ResumoResponse(
        YearMonth mes,
        BigDecimal totalEntradas,
        BigDecimal totalSaidas,
        BigDecimal saldo,
        List<TotalPorCategoria> porCategoria
) {
}
