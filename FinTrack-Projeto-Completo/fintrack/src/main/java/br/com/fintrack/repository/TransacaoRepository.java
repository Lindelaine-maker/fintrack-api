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
    @Query("select t from Transacao t where t.usuario.id=:uid and t.data between :inicio and :fim order by t.data desc")
    List<Transacao> findByUsuarioIdAndDataBetween(@Param("uid") Long uid, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
    @Query("select coalesce(sum(t.valor),0) from Transacao t where t.usuario.id=:uid and t.tipo=:tipo")
    BigDecimal sumValorByUsuarioIdAndTipo(@Param("uid") Long uid, @Param("tipo") TipoTransacao tipo);
    @Query("select t from Transacao t where t.usuario.id=:uid and t.categoria.id=:cid order by t.data desc")
    List<Transacao> findByUsuarioIdAndCategoriaId(@Param("uid") Long uid, @Param("cid") Long cid);
    Optional<Transacao> findByIdAndUsuarioId(Long id, Long usuarioId);
    @Query("select coalesce(sum(t.valor),0) from Transacao t where t.usuario.id=:uid and t.tipo=:tipo and t.id <> :id")
    BigDecimal sumValorByUsuarioIdAndTipoExcludingId(@Param("uid") Long uid, @Param("tipo") TipoTransacao tipo, @Param("id") Long id);
}
