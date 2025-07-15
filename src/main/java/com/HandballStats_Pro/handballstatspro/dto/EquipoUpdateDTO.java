package com.HandballStats_Pro.handballstatspro.dto;

import com.HandballStats_Pro.handballstatspro.enums.Sexo;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EquipoUpdateDTO {
    @Size(max = 100)
    private String nombre;

    @Size(max = 50)
    private String categoria;

    private Long idClub; // solo Admin o GestorClub

    private String temporada;

    private Sexo sexo;
}
