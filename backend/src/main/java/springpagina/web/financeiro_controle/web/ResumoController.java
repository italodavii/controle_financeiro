package springpagina.web.financeiro_controle.web;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springpagina.web.financeiro_controle.dto.ResumoResponse;
import springpagina.web.financeiro_controle.service.TransacaoService;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/resumo")
public class ResumoController {

    private final TransacaoService service;

    public ResumoController(TransacaoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ResumoResponse> resumo(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {

        return ResponseEntity.ok(service.calcularResumo(mes));
    }
}
