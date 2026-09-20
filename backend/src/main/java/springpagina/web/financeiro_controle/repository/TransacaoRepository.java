package springpagina.web.financeiro_controle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springpagina.web.financeiro_controle.domain.Transacao;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
}
