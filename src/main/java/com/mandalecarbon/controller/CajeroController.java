package com.mandalecarbon.controller;

import static com.mandalecarbon.model.Datos.fila;

import com.mandalecarbon.model.Datos.Item;
import com.mandalecarbon.model.Datos.Kpi;
import com.mandalecarbon.model.Datos.Fila;
import com.mandalecarbon.repository.RestauranteRepository;
import com.mandalecarbon.repository.VentaRepository;
import com.mandalecarbon.model.Venta;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

/** Panel del cajero (escritorio). */
@Controller
@RequestMapping("/cajero")
public class CajeroController {
    private final RestauranteRepository restaurante;
    private final VentaRepository ventas;
    public CajeroController(RestauranteRepository restaurante, VentaRepository ventas) { this.restaurante = restaurante; this.ventas=ventas; }

    @ModelAttribute("portal")
    String portal() { return "PORTAL CAJERO"; }

    @ModelAttribute("menu")
    List<Item> menu() {
        return List.of(new Item("Cobros pendientes", "/cajero?estado=Pendiente"), new Item("Comprobantes pagados", "/cajero?estado=Pagado"),
                new Item("Cierre de caja", "/cajero/cierre"));
    }

    @GetMapping
    String cobros(@RequestParam(required=false) String estado, @RequestParam(required=false) String q, Model m) {
        List<com.mandalecarbon.model.Datos.Pedido> pedidos=restaurante.pedidosRecientes();
        long pendientes=pedidos.stream().filter(p->"Pendiente".equals(p.estadoPago())).count();
        long pagados=pedidos.stream().filter(p->"Pagado".equals(p.estadoPago())).count();
        m.addAttribute("kpis", List.of(new Kpi("PENDIENTES", String.valueOf(pendientes), "Pedidos por cobrar"), new Kpi("POR VALIDAR", String.valueOf(pedidos.stream().filter(p->"Por validar".equals(p.estadoPago())).count()), "Pagos digitales"),
                new Kpi("PAGADOS", String.valueOf(pagados), "Pedidos registrados"), new Kpi("TOTAL PEDIDOS", String.valueOf(pedidos.size()), "En base de datos")));
        m.addAttribute("cabeceras", List.of("Pedido", "Cliente", "Canal", "Estado operativo", "Método", "Estado de pago", "Total", "Acción"));
        List<Fila> filas=pedidos.stream()
                .filter(p -> estado == null || estado.isBlank() || estado.equals(p.estadoPago()))
                .filter(p -> q == null || q.isBlank() || (p.codigo()+" "+p.cliente()+" "+p.canal()).toLowerCase().contains(q.trim().toLowerCase()))
                .map(p->fila("Pagado".equals(p.estadoPago())?"Ver comprobante":"Cobrar","/cajero/cobro/"+p.codigo(),"#"+p.codigo(),p.cliente(),p.canal(),p.estadoOp(),p.metodo()==null?"—":p.metodo(),p.estadoPago(),p.total())).toList();
        m.addAttribute("filas", filas); m.addAttribute("estadoFiltro", estado); m.addAttribute("q", q);
        m.addAttribute("activoCajero", "Pagado".equals(estado) ? "Comprobantes pagados" : "Cobros pendientes");
        m.addAttribute("ayuda", filas.isEmpty() ? "No hay pedidos con esos filtros." : null);
        return "cajero/cobros";
    }

    @GetMapping("/cobro/{codigo}")
    String cobro(@PathVariable String codigo, Model m) {
        var comprobante = restaurante.comprobante(codigo);
        m.addAttribute("pedido", restaurante.pedido(codigo));
        m.addAttribute("items", restaurante.itemsPedido(codigo));
        m.addAttribute("comprobante", comprobante);
        m.addAttribute("activoCajero", comprobante == null ? "Cobros pendientes" : "Comprobantes pagados");
        return "cajero/cobro";
    }

    @PostMapping("/cobro/{codigo}")
    String registrarPago(@PathVariable String codigo, @RequestParam String medio,
                         @RequestParam(defaultValue="TICKET") String comprobante,
                         org.springframework.web.servlet.mvc.support.RedirectAttributes flash) {
        try {
            restaurante.registrarPago(codigo, medio, comprobante);
            flash.addFlashAttribute("mensajeCobro", "Pago registrado. El comprobante quedó guardado en el historial.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("errorCobro", e.getMessage());
        }
        return "redirect:/cajero/cobro/" + codigo;
    }

    @GetMapping("/cierre")
    String cierre(Model m) {
        LocalDate hoy=LocalDate.now();
        List<Venta> hoyVentas=ventas.findByFechaPagoGreaterThanEqualAndFechaPagoLessThanOrderByFechaPagoDesc(hoy.atStartOfDay(),hoy.plusDays(1).atStartOfDay());
        BigDecimal total=hoyVentas.stream().map(Venta::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add);
        Map<String, List<Venta>> porMedio=new LinkedHashMap<>();
        List.of("EFECTIVO","YAPE","PLIN","TARJETA").forEach(medio->porMedio.put(medio,new java.util.ArrayList<>()));
        hoyVentas.forEach(v->porMedio.computeIfAbsent(v.getMedioPago(),k->new java.util.ArrayList<>()).add(v));
        BigDecimal efectivo=porMedio.get("EFECTIVO").stream().map(Venta::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal digital=java.util.stream.Stream.concat(porMedio.get("YAPE").stream(),porMedio.get("PLIN").stream()).map(Venta::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal tarjeta=porMedio.get("TARJETA").stream().map(Venta::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add);
        m.addAttribute("kpis", List.of(new Kpi("VENTA REGISTRADA", soles(total), hoyVentas.size()+" cobros hoy"),
                new Kpi("EFECTIVO", soles(efectivo), porMedio.get("EFECTIVO").size()+" operaciones"),
                new Kpi("PAGOS DIGITALES", soles(digital), (porMedio.get("YAPE").size()+porMedio.get("PLIN").size())+" operaciones"),
                new Kpi("TARJETA", soles(tarjeta), porMedio.get("TARJETA").size()+" operaciones")));
        m.addAttribute("cabeceras", List.of("Método", "Operaciones", "Importe"));
        m.addAttribute("filas", porMedio.entrySet().stream().map(e->fila(null,null,e.getKey(),String.valueOf(e.getValue().size()),soles(e.getValue().stream().map(Venta::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add)))).toList());
        return "cajero/cierre";
    }

    private static String soles(BigDecimal importe) { return "S/ " + importe.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(); }
}
