package springpagina.web.financeiro_controle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springpagina.web.financeiro_controle.domain.Transacao;

import java.time.LocalDate;
import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    List<Transacao> findByDataBetweenOrderByDataDescIdDesc(LocalDate inicio, LocalDate fim);
}
