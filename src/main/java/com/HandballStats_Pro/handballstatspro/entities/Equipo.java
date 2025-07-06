package com.HandballStats_Pro.handballstatspro.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Equipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipo")
    private Long idEquipo;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_club", referencedColumnName = "id_club", nullable = true)
    private Club club;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String categoria;

    @Column(name = "fecha_creacion_equipo", nullable = false)
    private LocalDateTime fechaCreacionEquipo;

    @Column(name = "temporada", nullable = false, length = 10)
    private String temporada;
}