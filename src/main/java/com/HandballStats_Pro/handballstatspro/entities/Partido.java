package com.HandballStats_Pro.handballstatspro.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "partido")
@Data
public class Partido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_partido")
    private Integer idPartido;

    @Column(name = "nombre_equipo_local", nullable = false)
    private String nombreEquipoLocal;

    @Column(name = "nombre_equipo_visitante", nullable = false)
    private String nombreEquipoVisitante;

    @Column(name = "id_equipo_local_asociado")
    private Long idEquipoLocalAsociado;

    @Column(name = "id_equipo_visitante_asociado")
    private Long idEquipoVisitanteAsociado;

    @Column(name = "resultado")
    private String resultado;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "id_usuario_registro")
    private Long idUsuarioRegistro;

    @Column(name = "competicion", length = 100)
    private String competicion;
}