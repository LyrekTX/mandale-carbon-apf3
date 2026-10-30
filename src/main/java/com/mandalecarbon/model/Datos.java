package com.mandalecarbon.model;

import java.util.Arrays;
import java.util.List;

/** Datos provisionales (sin base de datos) para los wireframes. */
public final class Datos {
    private Datos() {}

    public record Item(String etiqueta, String url) {}
    public record Kpi(String etiqueta, String valor, String nota) {}
    public record Campo(String etiqueta, String valor) {}
    public record Plato(String id, String nombre, String detalle, String precio, String estado) {
        public Plato(String id, String nombre, String detalle, String precio) {
            this(id, nombre, detalle, precio, "DISPONIBLE");
        }
    }
    public record LineaCarrito(Integer productoId, String nombre, Integer cantidad,
                              String precio, String subtotal) {}
    public record Pedido(String codigo, String canal, String cliente, String estadoOp,
                         String estadoPago, String hora, String total, String metodo) {}
    public record LineaPedido(String nombre, Integer cantidad, String subtotal, String observacion) {}
    public record Reserva(String codigo, String cliente, String fechaHora, Integer personas, String estado) {}
    public record Mesa(String nombre, String estado, String detalle) {}
    public record MesaSalon(Integer id, String nombre, String estado, String detalle, String codigoPedido) {}
    public record Comanda(String codigo, String origen, String tiempo, List<String> items) {}
    public record Columna(String titulo, List<Comanda> comandas) {}
    public record EntregaPanel(String codigo, String cliente, String telefono, String direccion,
                               String referencia, String estadoOp, String estadoPago, String total,
                               String hora, String estadoEntrega, boolean asignadaAMi, List<String> items) {}
    public record Comprobante(String tipo, String medio, String fecha, String cliente) {}
    public record ProductoForm(String nombre, String sku, String categoria, String precio, String descripcion, String disponibilidad) {}
    public record Barra(String dia, int pct, String monto) {}
    public record Fila(List<String> celdas, String accion, String url) {}

    public static Fila fila(String accion, String url, String... celdas) {
        return new Fila(Arrays.asList(celdas), accion, url);
    }

    public static final List<String> ESTADOS_OPERATIVOS = List.of(
            "Por aceptar", "En preparación", "Listo para recoger",
            "Listo para llevar", "En camino", "Entregado");

    public static final List<Plato> PLATOS = List.of(
            new Plato("1", "Parrilla clásica", "Carne, pollo y chorizo · Disponible", "S/ 34.90"),
            new Plato("2", "Costillas al carbón", "Con papa y ensalada · Disponible", "S/ 39.90"),
            new Plato("3", "Combo familiar", "Para 4 personas · Disponible", "S/ 89.90"),
            new Plato("4", "Pollo a la brasa", "Cuarto con papas y ensalada", "S/ 29.90"),
            new Plato("5", "Papas fritas", "Porción mediana", "S/ 6.00"),
            new Plato("6", "Chicha morada", "Vaso 500 ml", "S/ 6.00"));

    public static Plato plato(String id) {
        return PLATOS.stream().filter(p -> p.id().equals(id)).findFirst().orElse(PLATOS.get(0));
    }

    public static final List<Pedido> PEDIDOS = List.of(
            new Pedido("MC-1048", "Delivery", "Andrea Torres", "En camino", "Pagado", "7:02 p. m.", "S/ 51.89", "Billetera digital"),
            new Pedido("MC-1051", "Mesa 01", "Salón", "En preparación", "Pendiente", "7:14 p. m.", "S/ 78.80", "Efectivo"),
            new Pedido("MC-1058", "Delivery", "Diana", "Por aceptar", "Pendiente", "7:32 p. m.", "S/ 64.90", "Transferencia"),
            new Pedido("MC-1044", "Recojo", "Andrea", "Listo para recoger", "Por validar", "6:48 p. m.", "S/ 38.00", "Tarjeta"),
            new Pedido("MC-0972", "Delivery", "Andrea Torres", "Entregado", "Pagado", "24 sep.", "S/ 38.90", "Efectivo"));

    public static Pedido pedido(String codigo) {
        return PEDIDOS.stream().filter(p -> p.codigo().equals(codigo)).findFirst().orElse(PEDIDOS.get(0));
    }

    public static final List<Mesa> MESAS = List.of(
            new Mesa("Mesa 01", "Ocupada", "Pedido #MC-1051 · 22 min"),
            new Mesa("Mesa 02", "Libre", "4 asientos"),
            new Mesa("Mesa 03", "Ocupada", "Pedido #MC-1055 · 35 min"),
            new Mesa("Mesa 04", "Reservada", "8:00 p. m. · Andrea"),
            new Mesa("Mesa 05", "Libre", "2 asientos"),
            new Mesa("Mesa 06", "Ocupada", "Pedido #MC-1057 · 8 min"),
            new Mesa("Mesa 07", "Libre", "6 asientos"),
            new Mesa("Mesa 08", "Reservada", "8:30 p. m. · Carlos"));

    public static final List<Columna> TABLERO = List.of(
            new Columna("Por aceptar", List.of(
                    new Comanda("MC-1058", "Delivery · 2 productos", "02 min", List.of("1 × Parrilla clásica", "2 × Chicha morada")),
                    new Comanda("MC-1059", "Mesa 05 · 1 producto", "04 min", List.of("1 × Costillas al carbón")))),
            new Columna("En preparación", List.of(
                    new Comanda("MC-1048", "Delivery · 2 productos", "18 min", List.of("1 × Parrilla clásica", "2 × Chicha morada", "Nota: sin cebolla")),
                    new Comanda("MC-1051", "Mesa 01 · 2 productos", "12 min", List.of("2 × Pollo a la brasa", "1 × Papas fritas")))),
            new Columna("Listo para recoger", List.of(
                    new Comanda("MC-1044", "Recojo · Andrea", "25 min", List.of("1 × Combo familiar", "4 × Gaseosa")))),
            new Columna("Listo para llevar", List.of(
                    new Comanda("MC-1042", "Delivery · asignado", "31 min", List.of("2 × Parrilla clásica")))));
}
