package com.HandballStats_Pro.handballstatspro.dto;

import lombok.Data;
import java.time.LocalDate;

import jakarta.validation.constraints.Size;

@Data
public class PartidoUpdateDTO {
    @Size(max = 10, message = "El resultado no puede exceder los 10 caracteres")
    private String resultado;
    private Long idEquipoPropio;
    @Size(max = 100, message = "El nombre del rival no puede exceder los 100 caracteres")
    private String nombreRival;
    private Boolean esLocal;
    private LocalDate fecha;
}