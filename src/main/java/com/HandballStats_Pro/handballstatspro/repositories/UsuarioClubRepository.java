package com.HandballStats_Pro.handballstatspro.repositories;

import com.HandballStats_Pro.handballstatspro.entities.Club;
import com.HandballStats_Pro.handballstatspro.entities.Usuario;
import com.HandballStats_Pro.handballstatspro.entities.UsuarioClub;
import com.HandballStats_Pro.handballstatspro.entities.UsuarioClubId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioClubRepository extends JpaRepository<UsuarioClub, UsuarioClubId> {
    // Encuentra todas las relaciones usuario–club para un usuario dado
    List<UsuarioClub> findByUsuario_IdUsuario(Long idUsuario);

    // Comprueba existencia de la relación para control de acceso
    boolean existsByUsuario_IdUsuarioAndClub_IdClub(Long idUsuario, Long idClub);

    void deleteByClub(Club club);

    List<UsuarioClub> findByClub(Club club);

    void deleteByUsuario(Usuario usuario);

}