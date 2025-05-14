package com.HandballStats_Pro.handballstatspro.entities;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarioequipo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioEquipo {
    @EmbeddedId
    private UsuarioEquipoId id;

    @MapsId("idUsuario")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario")
    private Usuario usuario;

    @MapsId("idEquipo")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_equipo", referencedColumnName = "id_equipo")
    private Equipo equipo;

    public UsuarioEquipo(Usuario usuario, Equipo equipo) {
        this.usuario = usuario;
        this.equipo = equipo;
        this.id = new UsuarioEquipoId(usuario.getIdUsuario(), equipo.getIdEquipo());
    }
}