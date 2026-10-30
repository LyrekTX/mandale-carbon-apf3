package com.mandalecarbon.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="usuario",schema="dbo")
public class Usuario {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_usuario") private Integer id;
 @Column(nullable=false,length=80) private String nombre;
 @Column(nullable=false,length=80) private String apellido;
 @Column(nullable=false,unique=true,length=120) private String correo;
 @Column(name="password_hash",nullable=false,length=255) private String passwordHash;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_rol",nullable=false) private RolEntidad rol;
 @Column(nullable=false,length=10) private String estado="ACTIVO";
 @Column(name="fecha_creacion",insertable=false,updatable=false) private LocalDateTime fechaCreacion;
 public Usuario(){}
 public Integer getId(){return id;} public String getNombre(){return nombre;} public void setNombre(String s){nombre=s;}
 public String getApellido(){return apellido;} public void setApellido(String s){apellido=s;}
 public String getCorreo(){return correo;} public void setCorreo(String s){correo=s;}
 public String getPasswordHash(){return passwordHash;} public void setPasswordHash(String s){passwordHash=s;}
 public RolEntidad getRol(){return rol;} public void setRol(RolEntidad r){rol=r;}
 public String getEstado(){return estado;} public void setEstado(String s){estado=s;}
}
