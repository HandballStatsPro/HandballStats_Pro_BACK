package com.HandballStats_Pro.handballstatspro.enums;

public enum DetalleFinalizacion {
    Lanzamiento_Exterior,
    Pivote,
    Penetracion,
    Extremo,
    _7m,
    Contragol,
    _1ª_oleada("1ª oleada"),
    _2ª_oleada("2ª oleada"),
    _3ª_oleada("3ª oleada");

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