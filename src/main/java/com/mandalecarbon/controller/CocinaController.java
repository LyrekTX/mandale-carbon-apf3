package com.mandalecarbon.controller;

import com.mandalecarbon.model.Datos.Item;
import com.mandalecarbon.repository.RestauranteRepository;
import com.mandalecarbon.model.Datos.Fila;
import static com.mandalecarbon.model.Datos.fila;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

/** Panel de cocina (escritorio). */
@Controller
@RequestMapping("/cocina")
public class CocinaController {
    private final RestauranteRepository restaurante;

    public CocinaController(RestauranteRepository restaurante) {
        this.restaurante = restaurante;
    }

    @ModelAttribute("portal")
    String portal() { return "PORTAL COCINA"; }

    @ModelAttribute("menu")
    List<Item> menu() {
        return List.of(new Item("Tablero de cocina", "/cocina"),
                new Item("Por aceptar", "/cocina?estado=Por%20aceptar"),
                new Item("En preparación", "/cocina?estado=En%20preparaci%C3%B3n"),
                new Item("Pedidos listos", "/cocina?estado=Listos"),
                new Item("Historial", "/cocina/historial"));
    }

    @GetMapping
    String tablero(@RequestParam(required = false) String estado, Model m) {
        var columnas = restaurante.comandasCocina(estado);
        if (estado != null && !estado.isBlank()) {
            columnas = columnas.stream().filter(c -> "Listos".equals(estado)
                    ? c.titulo().startsWith("Listo para") : c.titulo().equals(estado)).toList();
        }
        m.addAttribute("columnas", columnas);
        m.addAttribute("estadoFiltro", estado == null ? "" : estado);
        m.addAttribute("tabCocina", estado == null || estado.isBlank() ? "Tablero de cocina"
                : "Listos".equals(estado) ? "Pedidos listos" : estado);
        return "cocina/tablero";
    }

    @GetMapping("/historial")
    String historial(Model m) {
        List<Fila> filas = restaurante.historialCocina().stream()
                .map(p -> fila("Ver", "/cocina/comanda/" + p.codigo(), p.codigo(), p.cliente(), p.canal(),
                        p.estadoOp(), p.hora(), p.total())).toList();
        m.addAttribute("titulo", "Historial de cocina"); m.addAttribute("activo", "Historial");
        m.addAttribute("boton", "Volver al tablero"); m.addAttribute("botonUrl", "/cocina");
        m.addAttribute("cabeceras", List.of("Pedido", "Cliente", "Canal", "Estado", "Fecha", "Total"));
        m.addAttribute("filas", filas); m.addAttribute("filtros", null); m.addAttribute("kpis", null);
        m.addAttribute("subtabs", null); m.addAttribute("ayuda", "Pedidos entregados o cancelados."); m.addAttribute("tabActiva", null);
        return "gerente/lista";
    }

    @GetMapping("/comanda/{codigo}")
    String comanda(@PathVariable String codigo, Model m) {
        m.addAttribute("pedido", restaurante.pedido(codigo));
        m.addAttribute("items", restaurante.itemsPedido(codigo));
        m.addAttribute("observacion", restaurante.observacionPedido(codigo));
        return "cocina/comanda";
    }

    @PostMapping("/comanda/{codigo}/estado")
    String avanzar(@PathVariable String codigo, @RequestParam String estado) {
        var pedido = restaurante.pedido(codigo);
        if ("Por aceptar".equals(pedido.estadoOp()) && "En preparación".equals(estado)) {
            restaurante.actualizarEstado(codigo, estado);
        } else if ("En preparación".equals(pedido.estadoOp())
                && ("Recojo".equals(pedido.canal()) ? "Listo para recoger" : "Listo para llevar").equals(estado)) {
            restaurante.actualizarEstado(codigo, estado);
        }
        return "redirect:/cocina/comanda/" + codigo;
    }
}
