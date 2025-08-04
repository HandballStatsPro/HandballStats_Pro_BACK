package com.HandballStats_Pro.handballstatspro.dto;

import com.HandballStats_Pro.handballstatspro.enums.*;
import com.HandballStats_Pro.handballstatspro.validation.ValueOfEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccionDTO {

    @NotNull(message = "El ID del partido es obligatorio")
    private Integer idPartido;

    @NotNull(message = "El ID de posesión es obligatorio")
    private Integer idPosesion;

    @NotNull(message = "El equipo de la acción es obligatorio")
    @ValueOfEnum(enumClass = EquipoAccion.class, message = "Valor inválido para 'equipoAccion'.")
    private String equipoAccion;

    @NotNull(message = "El tipo de ataque es obligatorio")
    @ValueOfEnum(enumClass = TipoAtaque.class, message = "Valor inválido para 'tipoAtaque'.")
    private String tipoAtaque;

    @NotNull(message = "El origen de la acción es obligatorio")
    @ValueOfEnum(enumClass = OrigenAccion.class, message = "Valor inválido para 'origenAccion'.")
    private String origenAccion;

    @NotNull(message = "El evento es obligatorio")
    @ValueOfEnum(enumClass = Evento.class, message = "Valor inválido para 'evento'.")
    private String evento;

    // Campos opcionales (la validación solo se aplica si no son nulos)
    @ValueOfEnum(enumClass = DetalleFinalizacion.class, message = "Valor inválido para 'detalleFinalizacion'.")
    private String detalleFinalizacion;

    @ValueOfEnum(enumClass = ZonaLanzamiento.class, message = "Valor inválido para 'zonaLanzamiento'.")
    private String zonaLanzamiento;

    @ValueOfEnum(enumClass = DetalleEvento.class, message = "Valor inválido para 'detalleEvento'.")
    private String detalleEvento;

    // Este campo se calcula en el servicio, no se valida en la entrada.
    private Boolean cambioPosesion;
}