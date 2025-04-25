package com.HandballStats_Pro.handballstatspro.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String contraseña;
}