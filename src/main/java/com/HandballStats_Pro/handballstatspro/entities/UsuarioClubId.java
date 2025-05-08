package com.HandballStats_Pro.handballstatspro.entities;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioClubId implements Serializable {
    private Long idUsuario;
    private Long idClub;
}