package com.HandballStats_Pro.handballstatspro.dto;

import com.HandballStats_Pro.handballstatspro.enums.*;
import lombok.Data;

@Data
public class AccionResponseDTO {
    
    private Integer idAccion;
    private Integer idPartido;
    private Integer idPosesion;
    private EquipoAccion equipoAccion;
    private TipoAtaque tipoAtaque;
    private OrigenAccion origenAccion;
    private Evento evento;
    private DetalleFinalizacion detalleFinalizacion;
    private ZonaLanzamiento zonaLanzamiento;
    private DetalleEvento detalleEvento;
    private Boolean cambioPosesion;
    
    // Información del partido asociado
    private String nombreEquipoLocal;
    private String nombreEquipoVisitante;
}