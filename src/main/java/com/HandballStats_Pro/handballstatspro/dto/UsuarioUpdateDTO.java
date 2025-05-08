package com.HandballStats_Pro.handballstatspro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import com.HandballStats_Pro.handballstatspro.enums.Rol;
import lombok.Data;

@Data
public class UsuarioUpdateDTO {
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;

    @Email(message = "Formato de email inválido")
    private String email;

    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String contraseña;
    
    private Rol rol;
}