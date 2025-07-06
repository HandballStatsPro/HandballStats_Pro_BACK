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

    @Column(name = "resultado")
    private String resultado;

    @Column(name = "id_equipo_propio")
    private Long idEquipoPropio;

    @Column(name = "nombre_rival")
    private String nombreRival;

    @Column(name = "es_local")
    private Boolean esLocal;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "id_usuario_registro")
    private Long idUsuarioRegistro;

    @ManyToOne
    @JoinColumn(name = "id_equipo_propio", referencedColumnName = "id_equipo", insertable = false, updatable = false)
    private Equipo equipoPropio;

    @Column(name = "competicion", length = 100)
    private String competicion;
}