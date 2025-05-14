package com.HandballStats_Pro.handballstatspro.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class EquipoDTO {
    @NotBlank
    @Size(max = 100)
    private String nombre;

    @NotBlank
    @Size(max = 50)
    private String categoria;

    @NotBlank
    @Size(max = 50)
    private String competicion;

    private Long idClub; // opcional según rol
}