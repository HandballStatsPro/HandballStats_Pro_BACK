package com.HandballStats_Pro.handballstatspro.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class EquipoResponseDTO {
    private Long idEquipo;
    private Long idClub;
    private String clubNombre;
    private String nombre;
    private String categoria;
    private LocalDateTime fechaCreacionEquipo;
    private List<UsuarioSimpleDTO> entrenadores;
    private String temporada;
}