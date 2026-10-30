package com.mandalecarbon.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity @Table(name="pedido",schema="dbo")
public class PedidoEntidad{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_pedido") private Integer id;
 @Column(name="fecha_hora",insertable=false,updatable=false) private LocalDateTime fechaHora;
 @Column(nullable=false,length=12) private String canal;
 @Column(nullable=false,length=25) private String estado="Por aceptar";
 @Column(name="estado_pago",nullable=false,length=15) private String estadoPago="Pendiente";
 @Column(name="id_cliente") private Integer clienteId;
 @Column(name="id_mesa") private Integer mesaId;
 @Column(name="id_usuario",nullable=false) private Integer usuarioId;
 @Column(length=300) private String observacion;
 @Column(name="direccion_entrega",length=250) private String direccionEntrega;
 @Column(length=200) private String referencia;
 @Column(name="monto_total",nullable=false,precision=10,scale=2) private BigDecimal montoTotal=BigDecimal.ZERO;
 @OneToMany(mappedBy="pedido",cascade=CascadeType.ALL,orphanRemoval=true) private List<DetallePedido> detalles=new ArrayList<>();
 public PedidoEntidad(){}
 public Integer getId(){return id;} public String getEstado(){return estado;} public void setEstado(String s){estado=s;}
 public String getCanal(){return canal;} public void setCanal(String s){canal=s;}
 public void setClienteId(Integer id){clienteId=id;} public void setUsuarioId(Integer id){usuarioId=id;}
 public void setMesaId(Integer id){mesaId=id;} public void setObservacion(String texto){observacion=texto;}
 public void setDireccionEntrega(String texto){direccionEntrega=texto;} public void setReferencia(String texto){referencia=texto;}
 public String getEstadoPago(){return estadoPago;} public LocalDateTime getFechaHora(){return fechaHora;}
 public BigDecimal getMontoTotal(){return montoTotal;} public void agregar(DetallePedido d){d.setPedido(this);detalles.add(d);montoTotal=montoTotal.add(d.getSubtotal());}
}
