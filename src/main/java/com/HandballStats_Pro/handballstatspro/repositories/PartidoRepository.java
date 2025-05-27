package com.HandballStats_Pro.handballstatspro.repositories;

import com.HandballStats_Pro.handballstatspro.entities.Partido;
import com.HandballStats_Pro.handballstatspro.entities.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDate;

@Repository
public interface PartidoRepository extends JpaRepository<Partido, Integer> {
    List<Partido> findByIdEquipoPropio(Long idEquipoPropio);
    List<Partido> findByFecha(LocalDate fecha);

    // Consulta para obtener partidos de equipos vinculados a un gestor (por club)
    @Query("SELECT p FROM Partido p WHERE p.idEquipoPropio IN " +
           "(SELECT e.idEquipo FROM Equipo e WHERE e.club.idClub IN " +
           "(SELECT uc.club.idClub FROM UsuarioClub uc WHERE uc.usuario.idUsuario = :idUsuario))")
    List<Partido> findPartidosByGestorClub(@Param("idUsuario") Long idUsuario);

    // Consulta para obtener partidos de equipos vinculados a un entrenador
    @Query("SELECT p FROM Partido p WHERE p.idEquipoPropio IN " +
           "(SELECT ue.equipo.idEquipo FROM UsuarioEquipo ue WHERE ue.usuario.idUsuario = :idUsuario)")
    List<Partido> findPartidosByEntrenador(@Param("idUsuario") Long idUsuario);

    // Verificar si un equipo pertenece a clubes vinculados a un gestor
    @Query("SELECT COUNT(e) > 0 FROM Equipo e WHERE e.idEquipo = :idEquipo " +
           "AND e.club.idClub IN (SELECT uc.club.idClub FROM UsuarioClub uc WHERE uc.usuario.idUsuario = :idUsuario)")
    boolean existsEquipoInGestorClubes(@Param("idEquipo") Long idEquipo, @Param("idUsuario") Long idUsuario);

    // Verificar si un equipo está vinculado a un entrenador
    @Query("SELECT COUNT(ue) > 0 FROM UsuarioEquipo ue WHERE ue.equipo.idEquipo = :idEquipo AND ue.usuario.idUsuario = :idUsuario")
    boolean existsEquipoInEntrenador(@Param("idEquipo") Long idEquipo, @Param("idUsuario") Long idUsuario);

    // Obtener equipos disponibles para gestor de club
    @Query("SELECT e FROM Equipo e WHERE e.club.idClub IN " +
           "(SELECT uc.club.idClub FROM UsuarioClub uc WHERE uc.usuario.idUsuario = :idUsuario)")
    List<Equipo> findEquiposByGestorClub(@Param("idUsuario") Long idUsuario);

    // Obtener equipos disponibles para entrenador
    @Query("SELECT e FROM Equipo e WHERE e.idEquipo IN " +
           "(SELECT ue.equipo.idEquipo FROM UsuarioEquipo ue WHERE ue.usuario.idUsuario = :idUsuario)")
    List<Equipo> findEquiposByEntrenador(@Param("idUsuario") Long idUsuario);

    @Modifying
    @Query("UPDATE Partido p SET p.idUsuarioRegistro = 0 WHERE p.idUsuarioRegistro = :idUsuario")
    void updateIdUsuarioRegistroToZero(Long idUsuario);

    @Modifying
    @Query("DELETE FROM Partido p WHERE p.idEquipoPropio = :idEquipo")
    void deleteByEquipoPropioId(Long idEquipo);
}