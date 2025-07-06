package com.HandballStats_Pro.handballstatspro.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PartidoDTO {
    private Integer idPartido;

    private String resultado;

    @NotNull(message = "El id del equipo propio es obligatorio")
    private Long idEquipoPropio;

    @NotBlank(message = "El nombre del rival es obligatorio")
    @Size(max = 100, message = "El nombre del rival no puede exceder los 100 caracteres")
    private String nombreRival;

    @NotNull(message = "El indicador de localía es obligatorio")
    private Boolean esLocal;

    @NotNull(message = "La fecha del partido es obligatoria")
    private LocalDate fecha;

    private LocalDateTime fechaRegistro;

    private Long idUsuarioRegistro;
    
    private String competicion;
}