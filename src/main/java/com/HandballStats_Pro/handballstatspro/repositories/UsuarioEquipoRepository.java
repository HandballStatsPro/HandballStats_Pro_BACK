package com.HandballStats_Pro.handballstatspro.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.HandballStats_Pro.handballstatspro.entities.Equipo;
import com.HandballStats_Pro.handballstatspro.entities.UsuarioEquipo;
import com.HandballStats_Pro.handballstatspro.entities.UsuarioEquipoId;



@Repository
public interface UsuarioEquipoRepository extends JpaRepository<UsuarioEquipo, UsuarioEquipoId> {
    List<UsuarioEquipo> findByUsuario_IdUsuario(Long idUsuario);
    List<UsuarioEquipo> findByEquipo_IdEquipo(Long idEquipo);
    boolean existsByUsuario_IdUsuarioAndEquipo_IdEquipo(Long idUsuario, Long idEquipo);
    void deleteByEquipo(Equipo equipo);
    void deleteByUsuario_IdUsuario(Long idUsuario);
}