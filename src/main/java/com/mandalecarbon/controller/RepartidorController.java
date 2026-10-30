package com.mandalecarbon.controller;

import com.mandalecarbon.model.Datos.EntregaPanel;
import com.mandalecarbon.model.Datos.Item;
import com.mandalecarbon.repository.RestauranteRepository;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Panel de entregas conectado a pedido y entrega en SQL Server. */
@Controller
@RequestMapping("/repartidor")
public class RepartidorController {
    private final RestauranteRepository restaurante;

    public RepartidorController(RestauranteRepository restaurante) { this.restaurante = restaurante; }

    @ModelAttribute("portal")
    String portal() { return "PORTAL REPARTIDOR"; }

    @ModelAttribute("menu")
    List<Item> menu() {
        return List.of(new Item("Disponibles", "/repartidor?tab=Disponibles"),
                new Item("Mis entregas", "/repartidor?tab=Activas"),
                new Item("Historial", "/repartidor?tab=Historial"));
    }

    @GetMapping
    String entregas(@RequestParam(defaultValue="Disponibles") String tab, HttpSession sesion, Model m) {
        boolean esRepartidor = "repartidor".equals(sesion.getAttribute("rol"));
        int repartidorId = esRepartidor ? restaurante.asegurarRepartidor((Integer) sesion.getAttribute("usuarioId")) : -1;
        String pestaña = List.of("Disponibles", "Activas", "Historial").contains(tab) ? tab : "Disponibles";
        List<EntregaPanel> disponibles = restaurante.entregasRepartidor(repartidorId, "Disponibles");
        List<EntregaPanel> activas = restaurante.entregasRepartidor(repartidorId, "Activas");
        List<EntregaPanel> historial = restaurante.entregasRepartidor(repartidorId, "Historial");
        m.addAttribute("entregas", switch (pestaña) {
            case "Activas" -> activas;
            case "Historial" -> historial;
            default -> disponibles;
        });
        m.addAttribute("disponibles", disponibles.size());
        m.addAttribute("activas", activas.size());
        m.addAttribute("entregadas", historial.size());
        m.addAttribute("tabActiva", pestaña);
        m.addAttribute("menuActivo", "Activas".equals(pestaña) ? "Mis entregas" : pestaña);
        m.addAttribute("esRepartidor", esRepartidor);
        m.addAttribute("sinResultados", pestaña.equals("Disponibles") ? "No hay pedidos listos sin asignar."
                : pestaña.equals("Activas") ? "No tienes entregas activas." : "Todavía no hay entregas completadas.");
        return "repartidor/entregas";
    }

    @GetMapping("/entrega/{codigo}")
    String entrega(@PathVariable String codigo, HttpSession sesion, Model m) {
        boolean esRepartidor = "repartidor".equals(sesion.getAttribute("rol"));
        int repartidorId = esRepartidor ? restaurante.asegurarRepartidor((Integer) sesion.getAttribute("usuarioId")) : -1;
        EntregaPanel entrega = restaurante.entregaRepartidor(codigo, repartidorId);
        m.addAttribute("entrega", entrega);
        m.addAttribute("esRepartidor", esRepartidor);
        String direccion = entrega.direccion() == null ? "" : entrega.direccion();
        m.addAttribute("mapaUrl", "https://www.google.com/maps/search/?api=1&query=" + URLEncoder.encode(direccion, StandardCharsets.UTF_8));
        m.addAttribute("telefonoLimpio", entrega.telefono() == null ? "" : entrega.telefono().replaceAll("[^0-9+]", ""));
        return "repartidor/entrega";
    }

    @PostMapping("/entrega/{codigo}/tomar")
    String tomar(@PathVariable String codigo, HttpSession sesion, RedirectAttributes flash) {
        if (!"repartidor".equals(sesion.getAttribute("rol"))) return "redirect:/repartidor";
        int repartidorId = restaurante.asegurarRepartidor((Integer) sesion.getAttribute("usuarioId"));
        try { restaurante.tomarEntrega(codigo, repartidorId); }
        catch (IllegalArgumentException e) {
            flash.addFlashAttribute("errorEntrega", e.getMessage());
            return "redirect:/repartidor?tab=Disponibles";
        }
        return "redirect:/repartidor/entrega/" + codigo;
    }

    @PostMapping("/entrega/{codigo}/iniciar")
    String iniciar(@PathVariable String codigo, HttpSession sesion, RedirectAttributes flash) {
        if (!"repartidor".equals(sesion.getAttribute("rol"))) return "redirect:/repartidor";
        int repartidorId = restaurante.asegurarRepartidor((Integer) sesion.getAttribute("usuarioId"));
        try { restaurante.iniciarEntrega(codigo, repartidorId); }
        catch (IllegalArgumentException e) { flash.addFlashAttribute("errorEntrega", e.getMessage()); }
        return "redirect:/repartidor/entrega/" + codigo;
    }

    @PostMapping("/entrega/{codigo}/completar")
    String completar(@PathVariable String codigo, HttpSession sesion, RedirectAttributes flash) {
        if (!"repartidor".equals(sesion.getAttribute("rol"))) return "redirect:/repartidor";
        int repartidorId = restaurante.asegurarRepartidor((Integer) sesion.getAttribute("usuarioId"));
        try { restaurante.completarEntrega(codigo, repartidorId); }
        catch (IllegalArgumentException e) { flash.addFlashAttribute("errorEntrega", e.getMessage()); }
        return "redirect:/repartidor/entrega/" + codigo;
    }
}
