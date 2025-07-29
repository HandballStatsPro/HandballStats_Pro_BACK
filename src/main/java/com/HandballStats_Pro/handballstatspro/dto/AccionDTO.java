package com.HandballStats_Pro.handballstatspro.dto;

import com.HandballStats_Pro.handballstatspro.enums.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccionDTO {
    
    @NotNull(message = "El ID del partido es obligatorio")
    private Integer idPartido;
    
    @NotNull(message = "El ID de posesión es obligatorio")
    private Integer idPosesion;
    
    @NotNull(message = "El equipo de la acción es obligatorio")
    private EquipoAccion equipoAccion;
    
    @NotNull(message = "El tipo de ataque es obligatorio")
    private TipoAtaque tipoAtaque;
    
    @NotNull(message = "El origen de la acción es obligatorio")
    private OrigenAccion origenAccion;
    
    @NotNull(message = "El evento es obligatorio")
    private Evento evento;
    
    // Campos opcionales según las reglas
    private DetalleFinalizacion detalleFinalizacion;
    private ZonaLanzamiento zonaLanzamiento;
    private DetalleEvento detalleEvento;
    
    // Este campo se calcula automáticamente según las reglas
    private Boolean cambioPosesion;
}