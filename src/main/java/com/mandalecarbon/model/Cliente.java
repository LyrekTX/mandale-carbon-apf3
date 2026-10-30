package com.mandalecarbon.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "cliente", schema = "dbo")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer id;
    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nombre;
    @Pattern(regexp = "^$|[0-9+() -]{7,20}")
    @Column(length = 20)
    private String telefono;
    @Email
    @Size(max = 120)
    @Column(length = 120)
    private String correo;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    public Cliente() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
