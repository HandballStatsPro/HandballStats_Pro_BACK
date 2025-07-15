package com.HandballStats_Pro.handballstatspro.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Pattern;

@Data
public class PartidoResponseDTO {
    private Integer idPartido;
    private String nombreEquipoLocal;
    private String nombreEquipoVisitante;
    private Long idEquipoLocalAsociado;
    private Long idEquipoVisitanteAsociado;
    private LocalDate fecha;
    @Pattern(regexp = "^$|\\d+-\\d+$", message = "El formato del resultado debe ser 'numero-numero' o estar vacío")
    private String resultado;
    private String competicion;
    private LocalDateTime fechaRegistro;
    private Long idUsuarioRegistro;
}