package com.mandalecarbon.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="detalle_pedido",schema="dbo")
public class DetallePedido{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_detalle") private Integer id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_pedido",nullable=false) private PedidoEntidad pedido;
 @ManyToOne(fetch=FetchType.EAGER,optional=false) @JoinColumn(name="id_producto",nullable=false) private Producto producto;
 @Column(nullable=false) private Integer cantidad;
 @Column(name="precio_unitario",nullable=false,precision=10,scale=2) private BigDecimal precioUnitario;
 @Column(nullable=false,precision=10,scale=2) private BigDecimal subtotal;
 @Column(length=200) private String observacion;
 protected DetallePedido(){}
 public DetallePedido(Producto p,int cantidad){this.producto=p;this.cantidad=cantidad;this.precioUnitario=p.getPrecio();this.subtotal=precioUnitario.multiply(BigDecimal.valueOf(cantidad));}
 public void setPedido(PedidoEntidad p){pedido=p;} public BigDecimal getSubtotal(){return subtotal;}
}
