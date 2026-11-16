package br.com.fintrack.repository;

import br.com.fintrack.model.Transacao;
import br.com.fintrack.model.TipoTransacao;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransacaoRepository extends JpaRepository<Transacao,Long> {
    @Query("select t from Transacao t where t.usuario.id=:uid " +
            "and (:inicio is null or t.data >= :inicio) " +
            "and (:fim is null or t.data <= :fim) " +
            "and (:categoriaId is null or t.categoria.id = :categoriaId) order by t.data desc")
    List<Transacao> buscarComFiltros(@Param("uid") Long uid, @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim, @Param("categoriaId") Long categoriaId);

    @Query("select coalesce(sum(t.valor),0) from Transacao t where t.usuario.id=:uid and t.tipo=:tipo")
    BigDecimal sumValorByUsuarioIdAndTipo(@Param("uid") Long uid, @Param("tipo") TipoTransacao tipo);

    Optional<Transacao> findByIdAndUsuarioId(Long id, Long usuarioId);
    boolean existsByIdAndUsuarioId(Long id, Long usuarioId);
    boolean existsByCategoriaId(Long categoriaId);

    @Query("select coalesce(sum(t.valor),0) from Transacao t where t.usuario.id=:uid and t.tipo=:tipo and t.id <> :id")
    BigDecimal sumValorByUsuarioIdAndTipoExcludingId(@Param("uid") Long uid, @Param("tipo") TipoTransacao tipo, @Param("id") Long id);
}
