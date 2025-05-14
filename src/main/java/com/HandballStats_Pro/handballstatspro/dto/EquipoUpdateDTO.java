package com.HandballStats_Pro.handballstatspro.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EquipoUpdateDTO {
    @Size(max = 100)
    private String nombre;

    @Size(max = 50)
    private String categoria;

    @Size(max = 50)
    private String competicion;

    private Long idClub; // solo Admin o GestorClub
}
