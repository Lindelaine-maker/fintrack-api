package br.com.fintrack.repository;

import br.com.fintrack.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria,Long> {
    List<Categoria> findByUsuarioIdOrderByNomeAsc(Long usuarioId);
    Optional<Categoria> findByIdAndUsuarioId(Long id, Long usuarioId);
    boolean existsByIdAndUsuarioId(Long id, Long usuarioId);
    boolean existsByNomeIgnoreCaseAndUsuarioId(String nome, Long usuarioId);
    boolean existsByNomeIgnoreCaseAndUsuarioIdAndIdNot(String nome, Long usuarioId, Long id);

    @Query("select case when c.usuario.id=:uid then true else false end from Categoria c where c.id=:id")
    Optional<Boolean> pertenceAoUsuario(@Param("id") Long id, @Param("uid") Long uid);
}