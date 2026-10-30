package com.mandalecarbon.service;

import com.mandalecarbon.model.*;
import com.mandalecarbon.repository.*;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

@Service
public class PedidoService {
    private final PedidoJpaRepository pedidos;
    private final ProductoJpaRepository productos;
    private final ClienteJpaRepository clientes;
    private final UsuarioRepository usuarios;
    private final JdbcTemplate db;

    public PedidoService(PedidoJpaRepository p, ProductoJpaRepository pr, ClienteJpaRepository c, UsuarioRepository u, JdbcTemplate db) {
        pedidos = p;
        productos = pr;
        clientes = c;
        usuarios = u;
        this.db = db;
    }

    @Transactional
    public String crear(String nombre, String telefono, String canal, Integer usuarioId, List<Integer> productosIds,
                        java.util.Map<Integer, Integer> cantidades, Integer mesaId, String observacion,
                        String direccion, String referencia) {
        if (productosIds == null || productosIds.isEmpty())
            throw new IllegalArgumentException("Selecciona al menos un producto");
        if (!List.of("Delivery", "Recojo", "Salon").contains(canal)) throw new IllegalArgumentException("Canal inválido");
        if ("Salon".equals(canal) && mesaId == null) throw new IllegalArgumentException("Selecciona una mesa libre para el pedido de salón");
        if ("Delivery".equals(canal) && (direccion == null || direccion.isBlank())) throw new IllegalArgumentException("Ingresa la dirección de entrega.");
        Usuario usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> new IllegalStateException("No se encontró la cuenta activa"));
        Cliente c = new Cliente();
        c.setNombre(nombre);
        c.setTelefono(telefono);
        c = clientes.save(c);
        PedidoEntidad p = new PedidoEntidad();
        p.setCanal(canal);
        p.setEstado("Por aceptar");
        p.setClienteId(c.getId());
        p.setUsuarioId(usuario.getId());
        p.setMesaId(mesaId);
        p.setObservacion(observacion == null || observacion.isBlank() ? null : observacion.trim());
        p.setDireccionEntrega(direccion == null || direccion.isBlank() ? null : direccion.trim());
        p.setReferencia(referencia == null || referencia.isBlank() ? null : referencia.trim());
        PedidoEntidad pedido = pedidos.save(p);
        for (Integer id : productosIds) {
            int cantidad = cantidades.getOrDefault(id, 1);
            if (cantidad < 1 || cantidad > 50) throw new IllegalArgumentException("Cada cantidad debe estar entre 1 y 50");
            Producto producto = productos.findById(id).filter(x -> "DISPONIBLE".equals(x.getEstado()))
                    .orElseThrow(() -> new IllegalArgumentException("Producto no disponible"));
            pedido.agregar(new DetallePedido(producto, cantidad));
        }
        pedido = pedidos.save(pedido);
        if (mesaId != null) {
            int ocupada = db.update("UPDATE dbo.mesa SET estado='OCUPADA' WHERE id_mesa=? AND estado='DISPONIBLE'", mesaId);
            if (ocupada == 0) throw new IllegalArgumentException("La mesa elegida ya no está disponible");
        }
        return "MC-%04d".formatted(pedido.getId());
    }

    @Transactional
    public void marcarSalonAtendido(String codigo) {
        int id = Integer.parseInt(codigo.replace("MC-", ""));
        PedidoEntidad p = pedidos.findById(id).orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        if (!"Salon".equals(p.getCanal()) || !"Listo para llevar".equals(p.getEstado()))
            throw new IllegalArgumentException("El pedido de salón debe estar listo antes de marcarlo atendido.");
        p.setEstado("Entregado");
        db.update("UPDATE dbo.mesa SET estado='DISPONIBLE' WHERE id_mesa=(SELECT id_mesa FROM dbo.pedido WHERE id_pedido=?)", id);
    }
}
