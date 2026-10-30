package com.mandalecarbon.controller;

import com.mandalecarbon.model.Datos.Item;
import com.mandalecarbon.model.Datos.Kpi;
import com.mandalecarbon.model.Datos.Fila;
import com.mandalecarbon.model.Datos.MesaSalon;
import com.mandalecarbon.repository.RestauranteRepository;
import com.mandalecarbon.service.PedidoService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import static com.mandalecarbon.model.Datos.fila;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

/** Panel del mesero (escritorio). */
@Controller
@RequestMapping("/mesero")
public class MeseroController {
    private final RestauranteRepository restaurante;
    private final PedidoService pedidos;
    public MeseroController(RestauranteRepository restaurante, PedidoService pedidos) { this.restaurante = restaurante; this.pedidos=pedidos; }

    @ModelAttribute("portal")
    String portal() { return "PORTAL MESERO"; }

    @ModelAttribute("menu")
    List<Item> menu() {
        return List.of(new Item("Resumen", "/mesero"), new Item("Mesas", "/mesero/mesas"),
                new Item("Pedidos recientes", "/mesero/pedidos"), new Item("Nuevo pedido", "/mesero/crear"));
    }

    @GetMapping
    String resumen(Model m) {
        List<MesaSalon> mesas = restaurante.mesasSalon();
        List<com.mandalecarbon.model.Datos.Pedido> pedidos = restaurante.pedidosRecientes();
        long ocupadas = mesas.stream().filter(x -> "Ocupada".equals(x.estado())).count();
        long porAceptar = pedidos.stream().filter(x -> "Por aceptar".equals(x.estadoOp())).count();
        long preparacion = pedidos.stream().filter(x -> "En preparación".equals(x.estadoOp())).count();
        long listos = pedidos.stream().filter(x -> x.estadoOp().startsWith("Listo para")).count();
        m.addAttribute("kpis", List.of(
                new Kpi("MESAS OCUPADAS", ocupadas + " / " + mesas.size(), "Estado actual del salón"),
                new Kpi("POR ACEPTAR", String.valueOf(porAceptar), "Pedidos registrados"),
                new Kpi("EN PREPARACIÓN", String.valueOf(preparacion), "Pedidos en cocina"),
                new Kpi("LISTOS", String.valueOf(listos), "Recojo y delivery")));
        m.addAttribute("mesas", mesas.stream().limit(8).toList());
        return "mesero/resumen";
    }

    @GetMapping("/mesas")
    String mesas(Model m) {
        m.addAttribute("mesas", restaurante.mesasSalon());
        return "mesero/mesas";
    }

    @GetMapping("/crear")
    String crear(@RequestParam(required = false) Integer mesaId, Model m) {
        m.addAttribute("platos", restaurante.productosDisponibles());
        m.addAttribute("mesas", restaurante.mesasSalon().stream().filter(x -> "Libre".equals(x.estado())).toList());
        m.addAttribute("mesaId", mesaId);
        return "mesero/crear-pedido";
    }

    @GetMapping("/pedidos")
    String pedidos(Model m) {
        List<Fila> filas = restaurante.pedidosRecientes().stream()
                .map(p -> fila("Ver", "/mesero/pedidos/" + p.codigo(), p.codigo(), p.cliente(),
                        p.canal(), p.estadoOp(), p.hora(), p.total())).toList();
        m.addAttribute("titulo", "Pedidos recientes");
        m.addAttribute("activo", "Pedidos recientes");
        m.addAttribute("boton", "+ Nuevo pedido");
        m.addAttribute("botonUrl", "/mesero/crear");
        m.addAttribute("cabeceras", List.of("Código", "Cliente", "Canal", "Estado", "Fecha y hora", "Total", "Acción"));
        m.addAttribute("filas", filas);
        m.addAttribute("filtros", null);
        m.addAttribute("kpis", null);
        m.addAttribute("subtabs", null);
        m.addAttribute("ayuda", filas.isEmpty() ? "Todavía no hay pedidos registrados." : null);
        m.addAttribute("tabActiva", null);
        return "gerente/lista";
    }

    @GetMapping("/pedidos/{codigo}")
    String pedido(@PathVariable String codigo, Model m) {
        m.addAttribute("pedido", restaurante.pedido(codigo));
        m.addAttribute("items", restaurante.itemsPedido(codigo));
        m.addAttribute("observacion", restaurante.observacionPedido(codigo));
        return "mesero/pedido";
    }

    @PostMapping("/crear")
    String crearPedido(@RequestParam String nombre, @RequestParam(required=false) String telefono,
                       @RequestParam(defaultValue="Delivery") String canal,
                       @RequestParam(required=false) List<Integer> productoIds,
                       @RequestParam java.util.Map<String, String> campos,
                       @RequestParam(required=false) Integer mesaId,
                       @RequestParam(required=false) String observacion,
                       @RequestParam(required=false) String direccion,
                       @RequestParam(required=false) String referencia,
                       HttpSession sesion, org.springframework.web.servlet.mvc.support.RedirectAttributes flash) {
        if (productoIds == null || productoIds.isEmpty()) {
            flash.addFlashAttribute("errorPedido", "Selecciona al menos un producto.");
            return "redirect:/mesero/crear";
        }
        Integer usuarioId=(Integer)sesion.getAttribute("usuarioId");
        java.util.Map<Integer, Integer> cantidades = new java.util.HashMap<>();
        for (Integer id : productoIds) {
            try { cantidades.put(id, Integer.parseInt(campos.getOrDefault("cantidad_" + id, "1"))); }
            catch (NumberFormatException invalido) { cantidades.put(id, 0); }
        }
        String codigo;
        try { codigo = pedidos.crear(nombre, telefono, canal, usuarioId, productoIds, cantidades, mesaId, observacion, direccion, referencia); }
        catch (IllegalArgumentException e) {
            flash.addFlashAttribute("errorPedido", e.getMessage());
            return "redirect:/mesero/crear";
        }
        return "redirect:/mesero/pedidos/" + codigo;
    }

    @PostMapping("/pedidos/{codigo}/atendido")
    String marcarSalonAtendido(@PathVariable String codigo) {
        pedidos.marcarSalonAtendido(codigo);
        return "redirect:/mesero/pedidos/" + codigo;
    }
}
