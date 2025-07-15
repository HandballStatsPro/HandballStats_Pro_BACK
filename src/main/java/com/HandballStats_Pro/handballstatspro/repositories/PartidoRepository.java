package com.HandballStats_Pro.handballstatspro.repositories;

import com.HandballStats_Pro.handballstatspro.entities.Equipo;
import com.HandballStats_Pro.handballstatspro.entities.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PartidoRepository extends JpaRepository<Partido, Integer> {

    List<Partido> findByFecha(LocalDate fecha);

    // NUEVA consulta para obtener partidos donde el usuario gestiona AL MENOS UNO de los equipos
    @Query("SELECT p FROM Partido p WHERE p.idEquipoLocalAsociado IN :equipoIds OR p.idEquipoVisitanteAsociado IN :equipoIds")
    List<Partido> findPartidosByEquiposAsociados(@Param("equipoIds") List<Long> equipoIds);

    // Consulta para obtener partidos de equipos vinculados a un gestor (por club)
    @Query("SELECT p FROM Partido p WHERE p.idEquipoLocalAsociado IN " +
           "(SELECT e.idEquipo FROM Equipo e WHERE e.club.idClub IN " +
           "(SELECT uc.club.idClub FROM UsuarioClub uc WHERE uc.usuario.idUsuario = :idUsuario)) " +
           "OR p.idEquipoVisitanteAsociado IN " +
           "(SELECT e.idEquipo FROM Equipo e WHERE e.club.idClub IN " +
           "(SELECT uc.club.idClub FROM UsuarioClub uc WHERE uc.usuario.idUsuario = :idUsuario))")
    List<Partido> findPartidosByGestorClub(@Param("idUsuario") Long idUsuario);

    // Consulta para obtener partidos de equipos vinculados a un entrenador
    @Query("SELECT p FROM Partido p WHERE p.idEquipoLocalAsociado IN " +
           "(SELECT ue.equipo.idEquipo FROM UsuarioEquipo ue WHERE ue.usuario.idUsuario = :idUsuario) " +
           "OR p.idEquipoVisitanteAsociado IN " +
           "(SELECT ue.equipo.idEquipo FROM UsuarioEquipo ue WHERE ue.usuario.idUsuario = :idUsuario)")
    List<Partido> findPartidosByEntrenador(@Param("idUsuario") Long idUsuario);

    // Estas dos consultas de permisos siguen siendo válidas y las reutilizaremos
    @Query("SELECT COUNT(e) > 0 FROM Equipo e WHERE e.idEquipo = :idEquipo " +
           "AND e.club.idClub IN (SELECT uc.club.idClub FROM UsuarioClub uc WHERE uc.usuario.idUsuario = :idUsuario)")
    boolean existsEquipoInGestorClubes(@Param("idEquipo") Long idEquipo, @Param("idUsuario") Long idUsuario);

    @Query("SELECT COUNT(ue) > 0 FROM UsuarioEquipo ue WHERE ue.equipo.idEquipo = :idEquipo AND ue.usuario.idUsuario = :idUsuario")
    boolean existsEquipoInEntrenador(@Param("idEquipo") Long idEquipo, @Param("idUsuario") Long idUsuario);

    // Estas dos consultas de equipos disponibles también son válidas
    @Query("SELECT e FROM Equipo e WHERE e.club.idClub IN " +
           "(SELECT uc.club.idClub FROM UsuarioClub uc WHERE uc.usuario.idUsuario = :idUsuario)")
    List<Equipo> findEquiposByGestorClub(@Param("idUsuario") Long idUsuario);

    @Query("SELECT e FROM Equipo e WHERE e.idEquipo IN " +
           "(SELECT ue.equipo.idEquipo FROM UsuarioEquipo ue WHERE ue.usuario.idUsuario = :idUsuario)")
    List<Equipo> findEquiposByEntrenador(@Param("idUsuario") Long idUsuario);

    @Modifying
    @Query("UPDATE Partido p SET p.idUsuarioRegistro = 0 WHERE p.idUsuarioRegistro = :idUsuario")
    void updateIdUsuarioRegistroToZero(Long idUsuario);

    // NUEVA consulta para borrar partidos asociados a un equipo eliminado
    @Modifying
    @Query("DELETE FROM Partido p WHERE p.idEquipoLocalAsociado = :idEquipo OR p.idEquipoVisitanteAsociado = :idEquipo")
    void deleteByEquipoAsociadoId(@Param("idEquipo") Long idEquipo);

       /**
       * Busca partidos de scouting (sin equipos asociados) registrados por un usuario específico.
       */
       @Query("SELECT p FROM Partido p WHERE p.idUsuarioRegistro = :idUsuario AND p.idEquipoLocalAsociado IS NULL AND p.idEquipoVisitanteAsociado IS NULL")
       List<Partido> findScoutingPartidosByRegistrador(@Param("idUsuario") Long idUsuario);
}