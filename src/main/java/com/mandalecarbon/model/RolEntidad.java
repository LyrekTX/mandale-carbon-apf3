package com.mandalecarbon.model;
import jakarta.persistence.*;
@Entity @Table(name="rol",schema="dbo")
public class RolEntidad {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_rol") private Integer id;
 @Column(nullable=false,unique=true,length=30) private String nombre;
 protected RolEntidad(){} public Integer getId(){return id;} public String getNombre(){return nombre;}
}
