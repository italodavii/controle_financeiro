package springpagina.web.financeiro_controle.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import springpagina.web.financeiro_controle.domain.Categoria;
import springpagina.web.financeiro_controle.domain.TipoTransacao;
import springpagina.web.financeiro_controle.domain.Transacao;
import springpagina.web.financeiro_controle.repository.TransacaoRepository;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Component
public class CargaInicial implements CommandLineRunner {

    private final TransacaoRepository repository;

    public CargaInicial(TransacaoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        // O H2 grava em arquivo, entao os dados sobrevivem ao restart.
        // Sem esta guarda, a carga rodaria de novo e duplicaria tudo.
        if (repository.count() > 0) {
            return;
        }

        YearMonth mesAtual = YearMonth.now();
        YearMonth mesAnterior = mesAtual.minusMonths(1);

        List<Transacao> transacoes = new ArrayList<>();

        // Mes anterior
        transacoes.add(nova("Salario", "4200.00", TipoTransacao.ENTRADA, Categoria.SALARIO, mesAnterior, 5));
        transacoes.add(nova("Freela landing page", "850.00", TipoTransacao.ENTRADA, Categoria.FREELA, mesAnterior, 12));
        transacoes.add(nova("Aluguel", "1500.00", TipoTransacao.SAIDA, Categoria.MORADIA, mesAnterior, 10));
        transacoes.add(nova("Supermercado", "620.45", TipoTransacao.SAIDA, Categoria.ALIMENTACAO, mesAnterior, 8));
        transacoes.add(nova("Combustivel", "280.00", TipoTransacao.SAIDA, Categoria.TRANSPORTE, mesAnterior, 15));
        transacoes.add(nova("Plano de saude", "320.00", TipoTransacao.SAIDA, Categoria.SAUDE, mesAnterior, 20));
        transacoes.add(nova("Cinema", "68.00", TipoTransacao.SAIDA, Categoria.LAZER, mesAnterior, 22));

        // Mes atual
        transacoes.add(nova("Salario", "4200.00", TipoTransacao.ENTRADA, Categoria.SALARIO, mesAtual, 5));
        transacoes.add(nova("Rendimento CDB", "92.30", TipoTransacao.ENTRADA, Categoria.RENDIMENTO, mesAtual, 3));
        transacoes.add(nova("Venda de monitor usado", "150.00", TipoTransacao.ENTRADA, Categoria.OUTRAS_ENTRADAS, mesAtual, 7));
        transacoes.add(nova("Aluguel", "1500.00", TipoTransacao.SAIDA, Categoria.MORADIA, mesAtual, 10));
        transacoes.add(nova("Supermercado", "715.80", TipoTransacao.SAIDA, Categoria.ALIMENTACAO, mesAtual, 9));
        transacoes.add(nova("Curso de Java", "199.90", TipoTransacao.SAIDA, Categoria.EDUCACAO, mesAtual, 11));
        transacoes.add(nova("Corridas de aplicativo", "45.60", TipoTransacao.SAIDA, Categoria.TRANSPORTE, mesAtual, 14));
        transacoes.add(nova("Farmacia", "87.25", TipoTransacao.SAIDA, Categoria.OUTRAS_SAIDAS, mesAtual, 16));

        repository.saveAll(transacoes);
    }

    private Transacao nova(String descricao, String valor, TipoTransacao tipo,
                           Categoria categoria, YearMonth mes, int dia) {

        Transacao transacao = new Transacao();
        transacao.setDescricao(descricao);
        transacao.setValor(new BigDecimal(valor));
        transacao.setTipo(tipo);
        transacao.setCategoria(categoria);
        transacao.setData(mes.atDay(dia));
        return transacao;
    }
}
