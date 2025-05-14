package com.HandballStats_Pro.handballstatspro.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsuarioEquipoDTO {
    @NotNull
    private Long idUsuario;

    @NotNull
    private Long idEquipo;
}