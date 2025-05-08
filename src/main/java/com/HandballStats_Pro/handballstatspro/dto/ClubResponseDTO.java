package com.HandballStats_Pro.handballstatspro.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ClubResponseDTO {
    private Long idClub;
    private String nombre;
    private String ciudad;
    private LocalDateTime fechaCreacionClub;
    private List<UsuarioSimpleDTO> gestores;
}
