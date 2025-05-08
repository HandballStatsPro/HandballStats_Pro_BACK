package com.HandballStats_Pro.handballstatspro.repositories;

import com.HandballStats_Pro.handballstatspro.entities.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {
    Optional<Club> findByNombre(String nombre);
}