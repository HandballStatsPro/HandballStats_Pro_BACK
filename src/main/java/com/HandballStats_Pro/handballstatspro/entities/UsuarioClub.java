package com.HandballStats_Pro.handballstatspro.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarioclub")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioClub {

    @EmbeddedId
    private UsuarioClubId id;

    @MapsId("idUsuario")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario")
    private Usuario usuario;

    @MapsId("idClub")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_club", referencedColumnName = "id_club")
    private Club club;

    // Constructor de conveniencia
    public UsuarioClub(Usuario usuario, Club club) {
        this.usuario = usuario;
        this.club = club;
        this.id = new UsuarioClubId(usuario.getIdUsuario(), club.getIdClub());
    }
}