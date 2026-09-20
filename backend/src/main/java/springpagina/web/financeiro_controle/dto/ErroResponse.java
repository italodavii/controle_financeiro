package springpagina.web.financeiro_controle.dto;

import java.util.Map;

public record ErroResponse(
        String mensagem,
        Map<String, String> campos
) {
}
