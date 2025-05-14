package com.HandballStats_Pro.handballstatspro.entities;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioEquipoId implements java.io.Serializable {
    private Long idUsuario;
    private Long idEquipo;
}