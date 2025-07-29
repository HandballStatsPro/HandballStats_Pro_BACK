package com.HandballStats_Pro.handballstatspro.repositories;

import com.HandballStats_Pro.handballstatspro.entities.Accion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccionRepository extends JpaRepository<Accion, Integer> {

    // Encontrar todas las acciones de un partido ordenadas por id (cronológicamente)
    List<Accion> findByIdPartidoOrderByIdAccionAsc(Integer idPartido);

    // Encontrar la última acción registrada en un partido
    @Query("SELECT a FROM Accion a WHERE a.idPartido = :idPartido ORDER BY a.idAccion DESC LIMIT 1")
    Optional<Accion> findUltimaAccionDelPartido(@Param("idPartido") Integer idPartido);

    // Encontrar la acción anterior a una determinada en un partido
    @Query("SELECT a FROM Accion a WHERE a.idPartido = :idPartido AND a.idAccion < :idAccion ORDER BY a.idAccion DESC LIMIT 1")
    Optional<Accion> findAccionAnterior(@Param("idPartido") Integer idPartido, @Param("idAccion") Integer idAccion);

    // Contar el número de acciones en un partido
    long countByIdPartido(Integer idPartido);

    // Verificar si existe un partido
    @Query("SELECT COUNT(p) > 0 FROM Partido p WHERE p.idPartido = :idPartido")
    boolean existsPartidoById(@Param("idPartido") Integer idPartido);
}