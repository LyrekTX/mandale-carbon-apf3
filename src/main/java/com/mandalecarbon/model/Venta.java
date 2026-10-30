package com.mandalecarbon.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="venta",schema="dbo")
public class Venta{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_venta") private Integer id;
 @Column(name="id_pedido",nullable=false,unique=true) private Integer pedidoId;
 @Column(name="tipo_comprobante",nullable=false,length=10) private String tipoComprobante;
 @Column(name="medio_pago",nullable=false,length=10) private String medioPago;
 @Column(name="monto_subtotal",nullable=false,precision=10,scale=2) private BigDecimal subtotal;
 @Column(name="monto_igv",nullable=false,precision=10,scale=2) private BigDecimal igv;
 @Column(name="monto_total",nullable=false,precision=10,scale=2) private BigDecimal total;
 @Column(name="fecha_pago",insertable=false,updatable=false) private LocalDateTime fechaPago;
 protected Venta(){}
 public Integer getId(){return id;} public Integer getPedidoId(){return pedidoId;} public String getMedioPago(){return medioPago;}
 public String getTipoComprobante(){return tipoComprobante;} public BigDecimal getTotal(){return total;} public LocalDateTime getFechaPago(){return fechaPago;}
}
