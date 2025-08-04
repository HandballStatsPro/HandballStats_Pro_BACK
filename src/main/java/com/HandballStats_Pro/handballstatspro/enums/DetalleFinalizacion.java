package com.HandballStats_Pro.handballstatspro.enums;

public enum DetalleFinalizacion {
    Lanzamiento_Exterior,
    Pivote,
    Penetracion,
    Extremo,
    _7m,
    Contragol,
    Primera_Oleada,
    Segunda_Oleada,
    Tercera_Oleada;

    private final String displayName;

    DetalleFinalizacion() {
        this.displayName = this.name();
    }

    DetalleFinalizacion(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}