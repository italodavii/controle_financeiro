package springpagina.web.financeiro_controle.domain;

public enum Categoria {

    SALARIO(TipoTransacao.ENTRADA, "Salário"),
    FREELA(TipoTransacao.ENTRADA, "Freela"),
    RENDIMENTO(TipoTransacao.ENTRADA, "Rendimento"),
    OUTRAS_ENTRADAS(TipoTransacao.ENTRADA, "Outras entradas"),

    ALIMENTACAO(TipoTransacao.SAIDA, "Alimentação"),
    TRANSPORTE(TipoTransacao.SAIDA, "Transporte"),
    MORADIA(TipoTransacao.SAIDA, "Moradia"),
    SAUDE(TipoTransacao.SAIDA, "Saúde"),
    LAZER(TipoTransacao.SAIDA, "Lazer"),
    EDUCACAO(TipoTransacao.SAIDA, "Educação"),
    OUTRAS_SAIDAS(TipoTransacao.SAIDA, "Outras saídas");

    private final TipoTransacao tipo;
    private final String rotulo;

    Categoria(TipoTransacao tipo, String rotulo) {
        this.tipo = tipo;
        this.rotulo = rotulo;
    }

    public TipoTransacao getTipo() {
        return tipo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
