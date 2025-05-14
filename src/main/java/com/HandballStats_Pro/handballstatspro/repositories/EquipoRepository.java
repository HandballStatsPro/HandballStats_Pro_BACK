package com.HandballStats_Pro.handballstatspro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.HandballStats_Pro.handballstatspro.entities.Equipo;
import java.util.List;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    List<Equipo> findByClub_IdClubIn(List<Long> clubIds);
}