package com.HandballStats_Pro.handballstatspro.entities;

import com.HandballStats_Pro.handballstatspro.enums.*;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "accion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Accion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_accion")
    private Integer idAccion;
    
    @Column(name = "id_partido", nullable = false)
    private Integer idPartido;
    
    @Column(name = "id_posesion", nullable = false)
    private Integer idPosesion;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "equipo_accion", nullable = false)
    private EquipoAccion equipoAccion;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ataque", nullable = false)
    private TipoAtaque tipoAtaque;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "origen_accion", nullable = false)
    private OrigenAccion origenAccion;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "evento", nullable = false)
    private Evento evento;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "detalle_finalizacion")
    private DetalleFinalizacion detalleFinalizacion;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "zona_lanzamiento")
    private ZonaLanzamiento zonaLanzamiento;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "detalle_evento")
    private DetalleEvento detalleEvento;
    
    @Column(name = "cambio_posesion", nullable = false)
    private Boolean cambioPosesion;
    
    // Relación con Partido
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_partido", insertable = false, updatable = false)
    private Partido partido;
}