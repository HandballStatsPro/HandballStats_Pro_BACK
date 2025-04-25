package com.HandballStats_Pro.handballstatspro.dto;

import com.HandballStats_Pro.handballstatspro.entities.Usuario;
import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private Usuario usuario;

    public AuthResponse(String token, Usuario usuario) {
        this.token = token;
        this.usuario = usuario;
    }
}