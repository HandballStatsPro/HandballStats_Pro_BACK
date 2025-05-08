package com.HandballStats_Pro.handballstatspro.dto;

import com.HandballStats_Pro.handballstatspro.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsuarioEmailDTO {
    private Long idUsuario;
    private String nombre;
    private String email;
    private Rol rol;
}
