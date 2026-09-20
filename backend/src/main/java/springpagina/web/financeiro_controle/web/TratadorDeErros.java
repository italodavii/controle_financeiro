package springpagina.web.financeiro_controle.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import springpagina.web.financeiro_controle.dto.ErroResponse;
import springpagina.web.financeiro_controle.exception.RegraNegocioException;
import springpagina.web.financeiro_controle.exception.TransacaoNaoEncontradaException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(TransacaoNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> naoEncontrada(TransacaoNaoEncontradaException e) {
        ErroResponse erro = new ErroResponse(e.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException e) {
        ErroResponse erro = new ErroResponse(e.getMessage(), null);
        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erroDeCampo : e.getBindingResult().getFieldErrors()) {
            campos.put(erroDeCampo.getField(), erroDeCampo.getDefaultMessage());
        }

        ErroResponse erro = new ErroResponse("Dados invalidos", campos);
        return ResponseEntity.badRequest().body(erro);
    }
}
