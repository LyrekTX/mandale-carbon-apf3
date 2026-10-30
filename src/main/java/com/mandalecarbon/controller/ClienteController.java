package com.mandalecarbon.controller;

import com.mandalecarbon.repository.RestauranteRepository;
import com.mandalecarbon.model.Datos.LineaCarrito;
import com.mandalecarbon.model.Datos.Plato;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Portal público y área del cliente (móvil). */
@Controller
public class ClienteController {
    private final RestauranteRepository restaurante;
    public ClienteController(RestauranteRepository restaurante) { this.restaurante = restaurante; }

    @ModelAttribute("carritoCantidad")
    int cantidadEnCarrito(HttpSession sesion) {
        return carrito(sesion).values().stream().mapToInt(Integer::intValue).sum();
    }

    @GetMapping("/")
    String inicio(Model m) {
        m.addAttribute("platos", restaurante.productosDisponibles().stream().limit(3).toList());
        return "cliente/inicio";
    }

    @GetMapping("/carta")
    String carta(@RequestParam(required = false) String q,
                 @RequestParam(required = false) String categoria,
                 @RequestParam(required = false) String rango,
                 @RequestParam(defaultValue = "true") boolean soloDisponibles,
                 Model m) {
        m.addAttribute("platos", restaurante.buscarProductosPublicos(q, categoria, rango, soloDisponibles));
        m.addAttribute("categorias", restaurante.categoriasProductos());
        m.addAttribute("q", q);
        m.addAttribute("categoria", categoria);
        m.addAttribute("rango", rango);
        m.addAttribute("soloDisponibles", soloDisponibles);
        return "cliente/carta";
    }

    @GetMapping("/producto/{id}")
    String producto(@PathVariable String id, Model m) {
        m.addAttribute("plato", restaurante.producto(id));
        return "cliente/producto";
    }

    @PostMapping("/carrito/agregar")
    String agregar(@RequestParam Integer productoId,
                   @RequestParam(defaultValue = "1") Integer cantidad,
                   @RequestParam(required = false) String termino,
                   @RequestParam(required = false) String nota,
                   HttpSession sesion, RedirectAttributes flash) {
        int cantidadAgregar = cantidad == null || cantidad < 1 || cantidad > 50 ? 1 : cantidad;
        restaurante.producto(productoId.toString()); // valida que exista y siga disponible
        carrito(sesion).merge(productoId, cantidadAgregar, (anterior, nueva) -> Math.min(50, anterior + nueva));
        Map<Integer, Map<String, String>> preferencias = preferencias(sesion);
        Map<String, String> detalle = new LinkedHashMap<>();
        if (termino != null && List.of("Medio", "3/4", "Bien cocido").contains(termino)) detalle.put("termino", termino);
        if (nota != null && !nota.isBlank()) detalle.put("nota", nota.trim().substring(0, Math.min(nota.trim().length(), 250)));
        if (detalle.isEmpty()) preferencias.remove(productoId);
        else preferencias.put(productoId, detalle);
        flash.addFlashAttribute("mensajeCarrito", "Producto agregado al carrito.");
        return "redirect:/carrito";
    }

    @GetMapping("/carrito")
    String carrito(HttpSession sesion, Model m) {
        cargarCarrito(sesion, m);
        return "cliente/carrito";
    }

    private void cargarCarrito(HttpSession sesion, Model m) {
        List<LineaCarrito> lineas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        Map<Integer, Integer> carrito = carrito(sesion);
        var iterador = carrito.entrySet().iterator();
        while (iterador.hasNext()) {
            var entrada = iterador.next();
            try {
                Plato plato = restaurante.producto(entrada.getKey().toString());
                BigDecimal precio = new BigDecimal(plato.precio().replace("S/ ", ""));
                BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(entrada.getValue())).setScale(2, RoundingMode.HALF_UP);
                lineas.add(new LineaCarrito(entrada.getKey(), plato.nombre(), entrada.getValue(),
                        soles(precio), soles(subtotal)));
                total = total.add(subtotal);
            } catch (IllegalArgumentException noDisponible) {
                iterador.remove();
            }
        }
        m.addAttribute("lineas", lineas);
        m.addAttribute("total", soles(total));
        m.addAttribute("preferencias", preferencias(sesion));
    }

    @PostMapping("/carrito/actualizar")
    String actualizarCarrito(@RequestParam Integer productoId, @RequestParam Integer cantidad,
                             HttpSession sesion) {
        if (cantidad == null || cantidad < 1) carrito(sesion).remove(productoId);
        else carrito(sesion).put(productoId, Math.min(cantidad, 50));
        return "redirect:/carrito";
    }

    @PostMapping("/carrito/quitar")
    String quitarDelCarrito(@RequestParam Integer productoId, HttpSession sesion) {
        carrito(sesion).remove(productoId);
        preferencias(sesion).remove(productoId);
        return "redirect:/carrito";
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, Integer> carrito(HttpSession sesion) {
        Object valor = sesion.getAttribute("carrito");
        if (valor instanceof Map<?, ?>) return (Map<Integer, Integer>) valor;
        Map<Integer, Integer> nuevo = new LinkedHashMap<>();
        sesion.setAttribute("carrito", nuevo);
        return nuevo;
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, Map<String, String>> preferencias(HttpSession sesion) {
        Object valor = sesion.getAttribute("preferenciasCarrito");
        if (valor instanceof Map<?, ?>) return (Map<Integer, Map<String, String>>) valor;
        Map<Integer, Map<String, String>> nuevo = new LinkedHashMap<>();
        sesion.setAttribute("preferenciasCarrito", nuevo);
        return nuevo;
    }

    private static String soles(BigDecimal importe) {
        return "S/ " + importe.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    // ---- Checkout (7 pasos) ----
    @GetMapping("/checkout/modalidad")
    String modalidad(HttpSession sesion, Model m) {
        cargarCarrito(sesion, m);
        return "cliente/checkout-modalidad";
    }

    @GetMapping("/checkout/identificacion")
    String identificacion(@RequestParam(defaultValue = "delivery") String modalidad, Model m, HttpSession sesion) {
        sesion.setAttribute("canalPedido", "recojo".equals(modalidad) ? "Recojo" : "Delivery");
        cargarCarrito(sesion, m);
        m.addAttribute("modalidad", modalidad);
        return "cliente/checkout-identificacion";
    }

    /** Según la modalidad elegida muestra dirección (delivery) o datos de recojo. */
    @GetMapping("/checkout/entrega")
    String entrega(@RequestParam(defaultValue = "delivery") String modalidad, HttpSession sesion, Model m) {
        sesion.setAttribute("canalPedido", "recojo".equals(modalidad) ? "Recojo" : "Delivery");
        cargarCarrito(sesion, m);
        return "recojo".equals(modalidad) ? "cliente/checkout-recojo" : "cliente/checkout-direccion";
    }

    @PostMapping("/checkout/entrega/guardar")
    String guardarEntrega(@RequestParam(required = false) String modalidad,
                          @RequestParam(required = false) String direccion,
                          @RequestParam(required = false) String distrito,
                          @RequestParam(required = false) String referencia,
                          HttpSession sesion, RedirectAttributes flash) {
        String canal = "recojo".equals(modalidad) ? "Recojo" : (String) sesion.getAttribute("canalPedido");
        if (!"Recojo".equals(canal)) {
            if (direccion == null || direccion.isBlank() || distrito == null || distrito.isBlank()) {
                flash.addFlashAttribute("errorEntrega", "Completa la dirección y el distrito.");
                return "redirect:/checkout/entrega?modalidad=delivery";
            }
            sesion.setAttribute("direccionEntrega", direccion.trim() + ", " + distrito.trim());
        } else {
            sesion.setAttribute("direccionEntrega", null);
        }
        sesion.setAttribute("canalPedido", "Recojo".equals(canal) ? "Recojo" : "Delivery");
        sesion.setAttribute("referenciaEntrega", referencia == null || referencia.isBlank() ? null : referencia.trim());
        return "redirect:/checkout/pago";
    }

    @GetMapping("/checkout/pago")
    String pago(HttpSession sesion, Model m) {
        cargarCarrito(sesion, m);
        return "cliente/checkout-pago";
    }

    @GetMapping("/checkout/revisar")
    String revisar(HttpSession sesion, Model m) {
        cargarCarrito(sesion, m);
        m.addAttribute("canalPedido", sesion.getAttribute("canalPedido") == null ? "Delivery" : sesion.getAttribute("canalPedido"));
        m.addAttribute("clienteNombre", sesion.getAttribute("clienteNombre"));
        m.addAttribute("clienteCorreo", sesion.getAttribute("clienteCorreo"));
        m.addAttribute("clienteTelefono", sesion.getAttribute("clienteTelefono"));
        return "cliente/checkout-revisar";
    }

    @PostMapping("/pedido/confirmar")
    String confirmarPedido(@RequestParam String nombre, @RequestParam(required = false) String telefono,
                           HttpSession sesion, RedirectAttributes flash) {
        Map<Integer, Integer> productos = carrito(sesion);
        if (productos.isEmpty()) return "redirect:/carrito";
        if (nombre == null || nombre.isBlank()) {
            flash.addFlashAttribute("errorPedido", "Ingresa tu nombre para registrar el pedido.");
            return "redirect:/checkout/revisar";
        }
        List<Integer> ids = new ArrayList<>(productos.keySet());
        List<Integer> cantidades = new ArrayList<>(productos.values());
        Map<Integer, Map<String, String>> preferencias = preferencias(sesion);
        List<String> observaciones = ids.stream().map(id -> {
            Map<String, String> detalle = preferencias.get(id);
            if (detalle == null || detalle.isEmpty()) return null;
            List<String> partes = new ArrayList<>();
            if (detalle.get("termino") != null) partes.add("Término: " + detalle.get("termino"));
            if (detalle.get("nota") != null) partes.add("Nota: " + detalle.get("nota"));
            return String.join(" · ", partes);
        }).toList();
        String canal = (String) sesion.getAttribute("canalPedido");
        if (!"Recojo".equals(canal)) canal = "Delivery";
        Integer usuarioGerente = restaurante.usuarioGerente();
        Integer clienteId = (Integer) sesion.getAttribute("clienteId");
        String nombrePedido = sesion.getAttribute("clienteNombre") instanceof String nombreCuenta ? nombreCuenta : nombre.trim();
        String codigo = restaurante.crearPedido(nombrePedido, telefono, canal, null, usuarioGerente, ids, cantidades,
                (String) sesion.getAttribute("direccionEntrega"), (String) sesion.getAttribute("referenciaEntrega"), clienteId, observaciones);
        sesion.setAttribute("ultimoPedido", codigo);
        @SuppressWarnings("unchecked")
        List<String> misPedidos = (List<String>) sesion.getAttribute("misPedidos");
        if (misPedidos == null) misPedidos = new ArrayList<>();
        misPedidos.add(codigo);
        sesion.setAttribute("misPedidos", misPedidos);
        productos.clear();
        preferencias.clear();
        return "redirect:/pedido/confirmado?codigo=" + codigo;
    }

    @GetMapping("/pedido/confirmado")
    String confirmado(@RequestParam(required = false) String codigo, HttpSession sesion, Model m) {
        String codigoPedido = codigo != null ? codigo : (String) sesion.getAttribute("ultimoPedido");
        m.addAttribute("codigoPedido", codigoPedido == null ? "" : codigoPedido);
        if (codigoPedido != null) m.addAttribute("pedido", restaurante.pedido(codigoPedido));
        return "cliente/pedido-confirmado";
    }

    @GetMapping("/seguimiento/{token}")
    String seguimiento(@PathVariable String token, Model m) {
        var pedido = restaurante.pedido(token);
        List<String> estados = new ArrayList<>(com.mandalecarbon.model.Datos.ESTADOS_OPERATIVOS);
        if ("Recojo".equals(pedido.canal())) estados.remove("En camino");
        m.addAttribute("pedido", pedido);
        m.addAttribute("estados", estados);
        m.addAttribute("indiceEstado", estados.indexOf(pedido.estadoOp()));
        m.addAttribute("token", token);
        return "cliente/seguimiento";
    }

    // ---- Reservas ----
    @GetMapping("/reservas")
    String reservas(Model m) {
        m.addAttribute("fechaMinima", java.time.LocalDate.now().toString());
        return "cliente/reservar";
    }

    @PostMapping("/reservas")
    String crearReserva(@RequestParam String nombre,
                        @RequestParam(required = false) String telefono,
                        @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fecha,
                        @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.TIME) java.time.LocalTime hora,
                        @RequestParam Integer personas,
                        @RequestParam(required = false) String nota,
                        RedirectAttributes flash) {
        java.time.LocalDateTime fechaHora = fecha.atTime(hora);
        if (nombre == null || nombre.isBlank() || personas < 1 || personas > 20
                || !fechaHora.isAfter(java.time.LocalDateTime.now())) {
            flash.addFlashAttribute("errorReserva", "Completa tus datos y elige una fecha y hora futuras (hasta 20 personas).");
            return "redirect:/reservas";
        }
        Integer id = restaurante.crearReserva(nombre.trim(), telefono, fechaHora, personas, nota);
        return "redirect:/reservas/confirmada?id=" + id;
    }

    @GetMapping("/reservas/confirmada")
    String reservaConfirmada(@RequestParam Integer id, Model m) {
        m.addAttribute("reserva", restaurante.reserva(id));
        return "cliente/reserva-confirmada";
    }

    // ---- Cuenta ----
    @GetMapping("/login")
    String login() { return "cliente/login"; }

    @GetMapping("/registro")
    String registro() { return "cliente/registro"; }

    @GetMapping("/cuenta")
    String cuenta(HttpSession sesion, Model m) {
        m.addAttribute("clienteId", sesion.getAttribute("clienteId"));
        m.addAttribute("clienteNombre", sesion.getAttribute("clienteNombre"));
        m.addAttribute("clienteCorreo", sesion.getAttribute("clienteCorreo"));
        return "cliente/cuenta";
    }

    @GetMapping("/cuenta/pedidos")
    String historial(HttpSession sesion, Model m) {
        @SuppressWarnings("unchecked")
        List<String> codigos = (List<String>) sesion.getAttribute("misPedidos");
        Integer clienteId = (Integer) sesion.getAttribute("clienteId");
        List<com.mandalecarbon.model.Datos.Pedido> pedidos = clienteId != null
                ? restaurante.pedidosCliente(clienteId)
                : codigos == null ? List.of() : codigos.stream().map(restaurante::pedido).toList();
        m.addAttribute("pedidos", pedidos);
        return "cliente/historial";
    }
}
