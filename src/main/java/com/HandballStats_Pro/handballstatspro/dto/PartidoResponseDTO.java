package com.HandballStats_Pro.handballstatspro.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PartidoResponseDTO {
    private Integer idPartido;
    private String resultado;
    private Long idEquipoPropio;
    private String nombreRival;
    private Boolean esLocal;
    private LocalDate fecha;
    private LocalDateTime fechaRegistro;
    private Long idUsuarioRegistro;
    private String competicion;
}