package com.HandballStats_Pro.handballstatspro.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsuarioClubDTO {
    @NotNull(message = "El ID de usuario es obligatorio")
    private Long idUsuario;

    @NotNull(message = "El ID de club es obligatorio")
    private Long idClub;
}