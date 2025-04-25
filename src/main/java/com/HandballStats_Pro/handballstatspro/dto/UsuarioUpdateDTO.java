package com.HandballStats_Pro.handballstatspro.dto;

import com.HandballStats_Pro.handballstatspro.enums.Rol;

import lombok.Data;

@Data
public class UsuarioUpdateDTO {
    private String nombre;
    private String email;
    private String contraseña;
    private Rol rol;
}
