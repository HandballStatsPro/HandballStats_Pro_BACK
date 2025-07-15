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
    
    // Buscar acciones por partido
    List<Accion> findByIdPartidoOrderByIdAccionAsc(Integer idPartido);
    
    // Buscar acciones por partido y posesión
    List<Accion> findByIdPartidoAndIdPosesionOrderByIdAccionAsc(Integer idPartido, Integer idPosesion);
    
    // Buscar la acción inmediatamente anterior en el mismo partido
    @Query("SELECT a FROM Accion a WHERE a.idPartido = :idPartido AND a.idAccion < :idAccion ORDER BY a.idAccion DESC")
    Optional<Accion> findPreviousActionInMatch(@Param("idPartido") Integer idPartido, @Param("idAccion") Integer idAccion);
    
    // Buscar la última acción en un partido (útil para validaciones secuenciales)
    @Query("SELECT a FROM Accion a WHERE a.idPartido = :idPartido ORDER BY a.idAccion DESC")
    Optional<Accion> findLastActionInMatch(@Param("idPartido") Integer idPartido);
    
    // Contar acciones por partido
    long countByIdPartido(Integer idPartido);
    
    // Eliminar acciones por partido (para cuando se elimina un partido)
    void deleteByIdPartido(Integer idPartido);
}