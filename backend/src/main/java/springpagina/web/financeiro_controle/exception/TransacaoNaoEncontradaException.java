package springpagina.web.financeiro_controle.exception;

public class TransacaoNaoEncontradaException extends RuntimeException {
    public TransacaoNaoEncontradaException(Long id) {
        super("Transacao nao encontrada: " + id);
    }
}
