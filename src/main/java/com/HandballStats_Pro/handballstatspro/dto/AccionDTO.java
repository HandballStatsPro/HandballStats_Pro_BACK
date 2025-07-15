package com.HandballStats_Pro.handballstatspro.dto;

import com.HandballStats_Pro.handballstatspro.enums.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccionDTO {
    
    @NotNull(message = "El ID del partido es obligatorio")
    private Integer idPartido;
    
    @NotNull(message = "El ID de la posesión es obligatorio")
    private Integer idPosesion;
    
    @NotNull(message = "El equipo de acción es obligatorio")
    private EquipoAccion equipoAccion;
    
    @NotNull(message = "El tipo de ataque es obligatorio")
    private TipoAtaque tipoAtaque;
    
    @NotNull(message = "El origen de la acción es obligatorio")
    private OrigenAccion origenAccion;
    
    @NotNull(message = "El evento es obligatorio")
    private Evento evento;
    
    private DetalleFinalizacion detalleFinalizacion;
    
    private ZonaLanzamiento zonaLanzamiento;
    
    private DetalleEvento detalleEvento;
    
    @NotNull(message = "El cambio de posesión es obligatorio")
    private Boolean cambioPosesion;
}