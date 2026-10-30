package com.mandalecarbon.model;

import jakarta.persistence.*;

@Entity
@Table(name="categoria_producto", schema="dbo")
public class CategoriaProducto {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_categoria")
    private Integer id;
    @Column(nullable=false, unique=true, length=40) private String nombre;
    @Column(length=150) private String descripcion;
    protected CategoriaProducto() {}
    public Integer getId(){return id;} public String getNombre(){return nombre;}
    public void setNombre(String nombre){this.nombre=nombre;} public String getDescripcion(){return descripcion;}
    public void setDescripcion(String descripcion){this.descripcion=descripcion;}
}
