package com.HandballStats_Pro.handballstatspro.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.Data;

@Data
public class PartidoDTO {
    @NotBlank private String nombreEquipoLocal;
    @NotBlank private String nombreEquipoVisitante;
    private Long idEquipoLocalAsociado;   // Opcional
    private Long idEquipoVisitanteAsociado; // Opcional
    @NotNull private LocalDate fecha;
    @Pattern(regexp = "^$|\\d+-\\d+$", message = "El formato del resultado debe ser 'numero-numero' o estar vacío")
    private String resultado;
    private String competicion;
}