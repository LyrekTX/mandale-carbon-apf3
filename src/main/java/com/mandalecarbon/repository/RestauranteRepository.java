package com.mandalecarbon.repository;

import com.mandalecarbon.model.Datos.Pedido;
import com.mandalecarbon.model.Datos.Plato;
import com.mandalecarbon.model.Datos.Reserva;
import com.mandalecarbon.model.Datos.Comanda;
import com.mandalecarbon.model.Datos.Columna;
import com.mandalecarbon.model.Datos.LineaPedido;
import com.mandalecarbon.model.Datos.MesaSalon;
import com.mandalecarbon.model.Datos.EntregaPanel;
import com.mandalecarbon.model.Datos.Comprobante;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Acceso SQL Server a las operaciones principales del restaurante. */
@Repository
public class RestauranteRepository {
    private final JdbcTemplate db;
    public RestauranteRepository(JdbcTemplate db) { this.db = db; }

    private static final RowMapper<Plato> PLATO = (rs, n) -> new Plato(
            Integer.toString(rs.getInt("id_producto")), rs.getString("nombre"),
            rs.getString("descripcion") == null ? "" : rs.getString("descripcion"),
            "S/ " + rs.getBigDecimal("precio").toPlainString(), rs.getString("estado"));
    private static final RowMapper<Pedido> PEDIDO = (rs, n) -> new Pedido(
            "MC-%04d".formatted(rs.getInt("id_pedido")), rs.getString("canal"),
            rs.getString("cliente_nombre"), rs.getString("estado"), rs.getString("estado_pago"),
            rs.getTimestamp("fecha_hora").toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            "S/ " + rs.getBigDecimal("monto_total").toPlainString(), rs.getString("medio_pago"));
    private static final RowMapper<Reserva> RESERVA = (rs, n) -> new Reserva(
            "RS-%04d".formatted(rs.getInt("id_reserva")), rs.getString("cliente_nombre"),
            rs.getTimestamp("fecha_hora").toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            rs.getInt("cantidad_personas"), rs.getString("estado"));

    public List<Plato> productosDisponibles() {
        return db.query("SELECT id_producto,nombre,descripcion,precio,estado FROM producto WHERE estado='DISPONIBLE' ORDER BY nombre", PLATO);
    }

    public List<Plato> buscarProductosPublicos(String q, String categoria, String rango, boolean soloDisponibles) {
        StringBuilder sql = new StringBuilder("SELECT p.id_producto,p.nombre,p.descripcion,p.precio,p.estado FROM dbo.producto p JOIN dbo.categoria_producto c ON c.id_categoria=p.id_categoria WHERE 1=1");
        List<Object> parametros = new ArrayList<>();
        if (soloDisponibles) sql.append(" AND p.estado='DISPONIBLE'");
        if (q != null && !q.isBlank()) {
            sql.append(" AND (p.nombre LIKE ? OR COALESCE(p.descripcion,'') LIKE ?)");
            String buscar = "%" + q.trim() + "%";
            parametros.add(buscar);
            parametros.add(buscar);
        }
        if (categoria != null && !categoria.isBlank()) {
            sql.append(" AND c.nombre=?");
            parametros.add(categoria);
        }
        switch (rango == null ? "" : rango) {
            case "hasta20" -> sql.append(" AND p.precio <= 20");
            case "20a40" -> sql.append(" AND p.precio > 20 AND p.precio <= 40");
            case "mas40" -> sql.append(" AND p.precio > 40");
            default -> { }
        }
        sql.append(" ORDER BY p.nombre");
        return db.query(sql.toString(), PLATO, parametros.toArray());
    }

    public List<String> categoriasProductos() {
        return db.queryForList("SELECT nombre FROM dbo.categoria_producto ORDER BY nombre", String.class);
    }

    public Plato producto(String id) {
        return db.query("SELECT id_producto,nombre,descripcion,precio,estado FROM producto WHERE id_producto=? AND estado='DISPONIBLE'", PLATO, Integer.parseInt(id))
                .stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
    }

    public List<Pedido> pedidosRecientes() { return db.query(selectPedido() + " ORDER BY p.fecha_hora DESC", PEDIDO); }
    public List<Pedido> pedidosCliente(Integer clienteId) {
        return db.query(selectPedido() + " WHERE p.id_cliente=? ORDER BY p.fecha_hora DESC", PEDIDO, clienteId);
    }
    public Pedido pedido(String codigo) { return db.query(selectPedido() + " WHERE p.id_pedido=?", PEDIDO, id(codigo)).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado")); }

    public List<LineaPedido> itemsPedido(String codigo) {
        return db.query("""
                SELECT pr.nombre,d.cantidad,d.subtotal,d.observacion
                FROM dbo.detalle_pedido d JOIN dbo.producto pr ON pr.id_producto=d.id_producto
                WHERE d.id_pedido=? ORDER BY d.id_detalle
                """, (rs, row) -> new LineaPedido(rs.getString("nombre"), rs.getInt("cantidad"),
                "S/ " + rs.getBigDecimal("subtotal").toPlainString(), rs.getString("observacion")), id(codigo));
    }

    public String observacionPedido(String codigo) {
        return db.query("SELECT observacion FROM dbo.pedido WHERE id_pedido=?", rs ->
                rs.next() ? rs.getString(1) : null, id(codigo));
    }

    public List<MesaSalon> mesasSalon() {
        return db.query("""
                SELECT m.id_mesa,m.numero_mesa,m.capacidad,
                       CASE WHEN p.id_pedido IS NOT NULL THEN 'Ocupada'
                            WHEN m.estado='RESERVADA' THEN 'Reservada' ELSE 'Libre' END estado_panel,
                       p.id_pedido,p.fecha_hora,c.nombre cliente_nombre
                FROM dbo.mesa m
                OUTER APPLY (
                  SELECT TOP (1) px.id_pedido,px.fecha_hora,px.id_cliente
                  FROM dbo.pedido px WHERE px.id_mesa=m.id_mesa
                    AND px.estado NOT IN ('Entregado','Cancelado')
                  ORDER BY px.fecha_hora DESC
                ) p
                LEFT JOIN dbo.cliente c ON c.id_cliente=p.id_cliente
                ORDER BY m.numero_mesa
                """, (rs, row) -> {
            int valorPedido = rs.getInt("id_pedido");
            Integer idPedido = rs.wasNull() ? null : valorPedido;
            String estado = rs.getString("estado_panel");
            String detalle = idPedido != null
                    ? "Pedido MC-%04d · %s".formatted(idPedido, rs.getString("cliente_nombre"))
                    : "Capacidad: " + rs.getInt("capacidad") + ("Reservada".equals(estado) ? " · Reservada" : " asientos");
            return new MesaSalon(rs.getInt("id_mesa"), "Mesa %02d".formatted(rs.getInt("numero_mesa")),
                    estado, detalle, idPedido == null ? null : "MC-%04d".formatted(idPedido));
        });
    }

    public List<Columna> comandasCocina(String filtro) {
        List<String> estados = List.of("Por aceptar", "En preparación", "Listo para recoger", "Listo para llevar");
        Map<String, List<Comanda>> porEstado = new LinkedHashMap<>();
        estados.forEach(estado -> porEstado.put(estado, new ArrayList<>()));
        String sql = selectPedido() + " WHERE p.estado IN ('Por aceptar','En preparación','Listo para recoger','Listo para llevar')";
        boolean estadoExacto = filtro != null && estados.contains(filtro);
        String condicion = estadoExacto ? " AND p.estado=?"
                : "Listos".equals(filtro) ? " AND p.estado IN ('Listo para recoger','Listo para llevar')" : "";
        List<Pedido> activos = db.query(sql + condicion + " ORDER BY p.fecha_hora", PEDIDO,
                estadoExacto ? new Object[]{filtro} : new Object[]{});
        for (Pedido pedido : activos) {
            List<String> items = db.query("""
                    SELECT CAST(d.cantidad AS VARCHAR(10)) + ' × ' + pr.nombre
                    FROM dbo.detalle_pedido d JOIN dbo.producto pr ON pr.id_producto=d.id_producto
                    WHERE d.id_pedido=? ORDER BY d.id_detalle
                    """, (rs, row) -> rs.getString(1), id(pedido.codigo()));
            Integer minutos = db.queryForObject("SELECT DATEDIFF(MINUTE,fecha_hora,SYSDATETIME()) FROM dbo.pedido WHERE id_pedido=?", Integer.class, id(pedido.codigo()));
            Comanda comanda = new Comanda(pedido.codigo(), pedido.canal() + " · " + pedido.cliente(),
                    Math.max(0, minutos == null ? 0 : minutos) + " min", items);
            porEstado.get(pedido.estadoOp()).add(comanda);
        }
        return estados.stream().map(estado -> new Columna(estado, porEstado.get(estado))).toList();
    }

    public List<Pedido> historialCocina() {
        return db.query(selectPedido() + " WHERE p.estado IN ('Entregado','Cancelado') ORDER BY p.fecha_hora DESC", PEDIDO);
    }

    public List<Reserva> reservasRecientes() {
        return db.query("""
                SELECT r.id_reserva,r.fecha_hora,r.cantidad_personas,r.estado,c.nombre cliente_nombre
                FROM dbo.reserva r JOIN dbo.cliente c ON c.id_cliente=r.id_cliente
                ORDER BY r.fecha_hora DESC
                """, RESERVA);
    }

    public Reserva reserva(Integer id) {
        return db.query("""
                SELECT r.id_reserva,r.fecha_hora,r.cantidad_personas,r.estado,c.nombre cliente_nombre
                FROM dbo.reserva r JOIN dbo.cliente c ON c.id_cliente=r.id_cliente
                WHERE r.id_reserva=?
                """, RESERVA, id).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
    }

    @Transactional
    public Integer crearReserva(String nombre, String telefono, LocalDateTime fechaHora,
                                Integer personas, String observacion) {
        Integer idCliente = db.queryForObject("INSERT INTO dbo.cliente(nombre,telefono) OUTPUT INSERTED.id_cliente VALUES(?,?)",
                Integer.class, nombre.trim(), telefono);
        return db.queryForObject("""
                INSERT INTO dbo.reserva(id_cliente,fecha_hora,cantidad_personas,observacion)
                OUTPUT INSERTED.id_reserva VALUES(?,?,?,?)
                """, Integer.class, idCliente, fechaHora, personas, observacion);
    }

    public Integer usuarioGerente() {
        return db.queryForObject("""
                SELECT TOP (1) u.id_usuario
                FROM dbo.usuario u JOIN dbo.rol r ON r.id_rol=u.id_rol
                WHERE r.nombre='GERENTE' AND u.estado='ACTIVO'
                ORDER BY u.id_usuario
                """, Integer.class);
    }

    @Transactional
    public String crearPedido(String nombre, String telefono, String canal, Integer idMesa,
                              Integer idUsuario, List<Integer> productos, List<Integer> cantidades,
                              String direccionEntrega, String referencia, Integer clienteRegistradoId) {
        return crearPedido(nombre, telefono, canal, idMesa, idUsuario, productos, cantidades,
                direccionEntrega, referencia, clienteRegistradoId,
                java.util.Collections.nCopies(productos == null ? 0 : productos.size(), null));
    }

    @Transactional
    public String crearPedido(String nombre, String telefono, String canal, Integer idMesa,
                              Integer idUsuario, List<Integer> productos, List<Integer> cantidades,
                              String direccionEntrega, String referencia, Integer clienteRegistradoId,
                              List<String> observaciones) {
        if (productos == null || productos.isEmpty() || productos.size() != cantidades.size()) throw new IllegalArgumentException("Agrega al menos un producto");
        Integer idCliente = clienteRegistradoId != null ? clienteRegistradoId
                : db.queryForObject("INSERT INTO dbo.cliente(nombre,telefono) OUTPUT INSERTED.id_cliente VALUES(?,?)", Integer.class, nombre, telefono);
        Integer idPedido = db.queryForObject("""
                INSERT INTO pedido(canal,id_cliente,id_mesa,id_usuario,estado,estado_pago,direccion_entrega,referencia)
                OUTPUT INSERTED.id_pedido VALUES(?,?,?,?,'Por aceptar','Pendiente',?,?)
                """, Integer.class, canal, idCliente, idMesa, idUsuario, direccionEntrega, referencia);
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < productos.size(); i++) {
            int cantidad = cantidades.get(i);
            if (cantidad < 1 || cantidad > 50) throw new IllegalArgumentException("Cantidad inválida");
            BigDecimal precio = db.queryForObject("SELECT precio FROM producto WHERE id_producto=? AND estado='DISPONIBLE'", BigDecimal.class, productos.get(i));
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(cantidad));
            String observacion = observaciones != null && i < observaciones.size() ? observaciones.get(i) : null;
            db.update("INSERT INTO detalle_pedido(id_pedido,id_producto,cantidad,precio_unitario,subtotal,observacion) VALUES(?,?,?,?,?,?)", idPedido, productos.get(i), cantidad, precio, subtotal, observacion);
            total = total.add(subtotal);
        }
        db.update("UPDATE pedido SET monto_total=? WHERE id_pedido=?", total, idPedido);
        return "MC-%04d".formatted(idPedido);
    }

    @Transactional
    public void actualizarEstado(String codigo, String estado) {
        int changed = db.update("UPDATE pedido SET estado=? WHERE id_pedido=?", estado, id(codigo));
        if (changed == 0) throw new IllegalArgumentException("Pedido no encontrado");
        if ("Listo para llevar".equals(estado)) {
            db.update("""
                    INSERT dbo.entrega(id_pedido)
                    SELECT p.id_pedido FROM dbo.pedido p
                    WHERE p.id_pedido=? AND p.canal='Delivery'
                      AND NOT EXISTS (SELECT 1 FROM dbo.entrega e WHERE e.id_pedido=p.id_pedido)
                    """, id(codigo));
        }
    }

    public Comprobante comprobante(String codigo) {
        return db.query("""
                SELECT v.tipo_comprobante,v.medio_pago,v.fecha_pago,c.nombre cliente_nombre
                FROM dbo.venta v JOIN dbo.pedido p ON p.id_pedido=v.id_pedido
                LEFT JOIN dbo.cliente c ON c.id_cliente=p.id_cliente WHERE v.id_pedido=?
                """, (rs, row) -> new Comprobante(rs.getString("tipo_comprobante"), rs.getString("medio_pago"),
                rs.getTimestamp("fecha_pago").toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                rs.getString("cliente_nombre")), id(codigo)).stream().findFirst().orElse(null);
    }

    public int asegurarRepartidor(Integer usuarioId) {
        db.update("INSERT dbo.repartidor(id_usuario) SELECT ? WHERE NOT EXISTS (SELECT 1 FROM dbo.repartidor WHERE id_usuario=?)",
                usuarioId, usuarioId);
        return db.queryForObject("SELECT id_repartidor FROM dbo.repartidor WHERE id_usuario=?", Integer.class, usuarioId);
    }

    public List<EntregaPanel> entregasRepartidor(int repartidorId, String pestaña) {
        db.update("""
                INSERT dbo.entrega(id_pedido)
                SELECT p.id_pedido FROM dbo.pedido p
                WHERE p.canal='Delivery' AND p.estado='Listo para llevar'
                  AND NOT EXISTS (SELECT 1 FROM dbo.entrega e WHERE e.id_pedido=p.id_pedido)
                """);
        String filtro = switch (pestaña == null ? "" : pestaña) {
            case "Activas" -> "e.id_repartidor=? AND e.estado IN ('Asignada','En camino')";
            case "Historial" -> "e.id_repartidor=? AND e.estado IN ('Entregada','Fallida')";
            default -> "e.id_repartidor IS NULL AND e.estado='Pendiente'";
        };
        Object[] args = filtro.contains("?") ? new Object[]{repartidorId} : new Object[]{};
        List<EntregaPanel> base = db.query("""
                SELECT p.id_pedido,p.canal,p.estado,p.estado_pago,p.fecha_hora,p.monto_total,
                       c.nombre cliente_nombre,c.telefono,p.direccion_entrega,p.referencia,e.estado estado_entrega,e.id_repartidor
                FROM dbo.entrega e JOIN dbo.pedido p ON p.id_pedido=e.id_pedido
                LEFT JOIN dbo.cliente c ON c.id_cliente=p.id_cliente
                WHERE
                """ + filtro + " ORDER BY p.fecha_hora", (rs, row) -> new EntregaPanel(
                "MC-%04d".formatted(rs.getInt("id_pedido")), rs.getString("cliente_nombre"), rs.getString("telefono"),
                rs.getString("direccion_entrega"), rs.getString("referencia"), rs.getString("estado"), rs.getString("estado_pago"),
                "S/ " + rs.getBigDecimal("monto_total").toPlainString(),
                rs.getTimestamp("fecha_hora").toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                rs.getString("estado_entrega"), rs.getInt("id_repartidor") == repartidorId, List.of()), args);
        return base.stream().map(e -> new EntregaPanel(e.codigo(), e.cliente(), e.telefono(), e.direccion(), e.referencia(),
                e.estadoOp(), e.estadoPago(), e.total(), e.hora(), e.estadoEntrega(), e.asignadaAMi(),
                db.query("""
                        SELECT CAST(d.cantidad AS VARCHAR(10)) + ' × ' + pr.nombre
                        FROM dbo.detalle_pedido d JOIN dbo.producto pr ON pr.id_producto=d.id_producto
                        WHERE d.id_pedido=? ORDER BY d.id_detalle
                        """, (rs, row) -> rs.getString(1), id(e.codigo())))).toList();
    }

    public EntregaPanel entregaRepartidor(String codigo, int repartidorId) {
        return entregasPorCodigo(codigo, "(ent.id_repartidor=? OR ent.id_repartidor IS NULL)", repartidorId);
    }

    private EntregaPanel entregasPorCodigo(String codigo, String condicion, int repartidorId) {
        EntregaPanel e = db.query("""
                SELECT p.id_pedido,p.estado,p.estado_pago,p.fecha_hora,p.monto_total,c.nombre cliente_nombre,c.telefono,
                       p.direccion_entrega,p.referencia,ent.estado estado_entrega,ent.id_repartidor
                FROM dbo.entrega ent JOIN dbo.pedido p ON p.id_pedido=ent.id_pedido
                LEFT JOIN dbo.cliente c ON c.id_cliente=p.id_cliente
                WHERE p.id_pedido=? AND
                """ + condicion, (rs, row) -> new EntregaPanel(
                "MC-%04d".formatted(rs.getInt("id_pedido")), rs.getString("cliente_nombre"), rs.getString("telefono"),
                rs.getString("direccion_entrega"), rs.getString("referencia"), rs.getString("estado"), rs.getString("estado_pago"),
                "S/ " + rs.getBigDecimal("monto_total").toPlainString(),
                rs.getTimestamp("fecha_hora").toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                rs.getString("estado_entrega"), rs.getInt("id_repartidor") == repartidorId, List.of()), id(codigo), repartidorId)
                .stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Entrega no encontrada o asignada a otro repartidor"));
        List<String> items = db.query("SELECT CAST(cantidad AS VARCHAR(10))+' × '+pr.nombre FROM dbo.detalle_pedido d JOIN dbo.producto pr ON pr.id_producto=d.id_producto WHERE id_pedido=? ORDER BY id_detalle",
                (rs, row) -> rs.getString(1), id(codigo));
        return new EntregaPanel(e.codigo(), e.cliente(), e.telefono(), e.direccion(), e.referencia(), e.estadoOp(), e.estadoPago(), e.total(), e.hora(), e.estadoEntrega(), e.asignadaAMi(), items);
    }

    public void tomarEntrega(String codigo, int repartidorId) {
        int n = db.update("UPDATE dbo.entrega SET id_repartidor=?,estado='Asignada' WHERE id_pedido=? AND id_repartidor IS NULL AND estado='Pendiente'",
                repartidorId, id(codigo));
        if (n == 0) throw new IllegalArgumentException("Esta entrega ya fue tomada o ya no está disponible.");
    }

    @Transactional
    public void iniciarEntrega(String codigo, int repartidorId) {
        int n = db.update("""
                UPDATE e SET estado='En camino',fecha_salida=COALESCE(fecha_salida,SYSDATETIME())
                FROM dbo.entrega e JOIN dbo.pedido p ON p.id_pedido=e.id_pedido
                WHERE e.id_pedido=? AND e.id_repartidor=? AND e.estado='Asignada' AND p.estado='Listo para llevar'
                """, id(codigo), repartidorId);
        if (n == 0) throw new IllegalArgumentException("La entrega no está asignada o no está lista.");
        db.update("UPDATE dbo.pedido SET estado='En camino' WHERE id_pedido=?", id(codigo));
    }

    @Transactional
    public void completarEntrega(String codigo, int repartidorId) {
        int n = db.update("UPDATE dbo.entrega SET estado='Entregada',fecha_entrega=SYSDATETIME() WHERE id_pedido=? AND id_repartidor=? AND estado='En camino'",
                id(codigo), repartidorId);
        if (n == 0) throw new IllegalArgumentException("Primero inicia el recorrido de esta entrega.");
        db.update("UPDATE dbo.pedido SET estado='Entregado' WHERE id_pedido=?", id(codigo));
    }

    @Transactional
    public void registrarPago(String codigo, String medio, String tipo) {
        if (!List.of("EFECTIVO", "YAPE", "PLIN", "TARJETA").contains(medio))
            throw new IllegalArgumentException("Selecciona un medio de pago válido.");
        if (!List.of("TICKET", "BOLETA").contains(tipo))
            throw new IllegalArgumentException("Selecciona Ticket o Boleta como tipo de comprobante.");
        int pedidoId = id(codigo);
        if (Boolean.TRUE.equals(db.queryForObject("SELECT CASE WHEN EXISTS(SELECT 1 FROM venta WHERE id_pedido=?) THEN CAST(1 AS bit) ELSE CAST(0 AS bit) END", Boolean.class, pedidoId)))
            throw new IllegalArgumentException("Este pedido ya tiene un pago registrado");
        String estadoPedido = db.query("SELECT estado FROM dbo.pedido WHERE id_pedido=?", rs -> rs.next() ? rs.getString(1) : null, pedidoId);
        if (estadoPedido == null) throw new IllegalArgumentException("Pedido no encontrado.");
        if ("Cancelado".equals(estadoPedido)) throw new IllegalArgumentException("No se puede cobrar un pedido cancelado.");
        BigDecimal total = db.queryForObject("SELECT monto_total FROM pedido WHERE id_pedido=?", BigDecimal.class, pedidoId);
        db.update("INSERT INTO venta(id_pedido,tipo_comprobante,medio_pago,monto_subtotal,monto_igv,monto_total) VALUES(?,?,?, ?,0,?)", pedidoId, tipo, medio, total, total);
        db.update("UPDATE pedido SET estado_pago='Pagado' WHERE id_pedido=?", pedidoId);
    }

    private static int id(String codigo) { return Integer.parseInt(codigo.replaceAll("\\D", "")); }
    private static String selectPedido() { return "SELECT p.id_pedido,p.canal,p.estado,p.estado_pago,p.fecha_hora,p.monto_total,v.medio_pago,COALESCE(c.nombre,'Cliente') cliente_nombre FROM pedido p LEFT JOIN cliente c ON c.id_cliente=p.id_cliente LEFT JOIN venta v ON v.id_pedido=p.id_pedido"; }
}
