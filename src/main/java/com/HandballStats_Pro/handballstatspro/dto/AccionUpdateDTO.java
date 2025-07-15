package com.HandballStats_Pro.handballstatspro.dto;

import com.HandballStats_Pro.handballstatspro.enums.*;
import lombok.Data;

@Data
public class AccionUpdateDTO {
    
    private EquipoAccion equipoAccion;
    private TipoAtaque tipoAtaque;
    private OrigenAccion origenAccion;
    private Evento evento;
    private DetalleFinalizacion detalleFinalizacion;
    private ZonaLanzamiento zonaLanzamiento;
    private DetalleEvento detalleEvento;
    private Boolean cambioPosesion;
}