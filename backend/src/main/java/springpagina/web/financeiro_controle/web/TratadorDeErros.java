package springpagina.web.financeiro_controle.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import springpagina.web.financeiro_controle.dto.ErroResponse;
import springpagina.web.financeiro_controle.exception.RegraNegocioException;
import springpagina.web.financeiro_controle.exception.TransacaoNaoEncontradaException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class TratadorDeErros {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

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

    /**
     * Parametro de URL que nao converte para o tipo esperado:
     * mes=abc, tipo=QUALQUER, id nao numerico.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> parametroInvalido(MethodArgumentTypeMismatchException e) {
        String mensagem = "Valor invalido para o parametro '" + e.getName() + "': " + e.getValue();

        ErroResponse erro = new ErroResponse(mensagem, null);
        return ResponseEntity.badRequest().body(erro);
    }

    /**
     * Corpo JSON que o Jackson nao consegue ler: enum inexistente,
     * data em formato errado.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException e) {
        ErroResponse erro = new ErroResponse("Corpo da requisicao invalido ou mal formatado", null);
        return ResponseEntity.badRequest().body(erro);
    }

    /**
     * Ultimo recurso: pega qualquer erro nao previsto para que a resposta
     * continue no formato ErroResponse. O detalhe fica so no log do
     * servidor; a resposta nao expoe mensagem interna nem stacktrace.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> inesperado(Exception e) {
        log.error("Erro interno nao tratado", e);

        ErroResponse erro = new ErroResponse("Erro interno inesperado", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }
}
