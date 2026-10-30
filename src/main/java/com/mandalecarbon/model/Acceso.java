package com.mandalecarbon.model;

import java.util.List;

/** Roles del personal (acceso de demostración, sin base de datos). */
public final class Acceso {
    private Acceso() {}

    public record Rol(String clave, String etiqueta, String nombre, String inicio) {}

    public static final List<Rol> ROLES = List.of(
            new Rol("gerente", "Gerente", "Gerente general", "/gerente"),
            new Rol("mesero", "Mesero", "Carlos Mendoza", "/mesero"),
            new Rol("cocina", "Cocina", "Rosa Díaz", "/cocina"),
            new Rol("cajero", "Cajero", "María León", "/cajero"),
            new Rol("repartidor", "Repartidor", "Luis Ramírez", "/repartidor"));

    public static Rol de(String clave) {
        return ROLES.stream().filter(r -> r.clave().equals(clave)).findFirst().orElse(ROLES.get(0));
    }
}
