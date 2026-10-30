package com.mandalecarbon.controller;

import static com.mandalecarbon.model.Datos.fila;

import com.mandalecarbon.model.Datos.Campo;
import com.mandalecarbon.model.Datos.Fila;
import com.mandalecarbon.model.Datos.Item;
import com.mandalecarbon.model.Datos.Kpi;
import com.mandalecarbon.model.Producto;
import com.mandalecarbon.service.ProductoService;
import com.mandalecarbon.service.ClienteService;
import com.mandalecarbon.service.ReporteService;
import com.mandalecarbon.service.InventarioService;
import com.mandalecarbon.model.Venta;
import com.mandalecarbon.model.Usuario;
import com.mandalecarbon.repository.UsuarioRepository;
import com.mandalecarbon.repository.RolRepository;
import com.mandalecarbon.model.RolEntidad;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.mandalecarbon.repository.RestauranteRepository;
import java.util.List;
import java.math.BigDecimal;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Dashboard y administración del gerente (escritorio). */
@Controller
@RequestMapping("/gerente")
public class GerenteController {
    private final ProductoService productos;
    private final ClienteService clientes;
    private final ReporteService reportes;
    private final RestauranteRepository restaurante;
    private final InventarioService inventario;
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder passwordEncoder;
    public GerenteController(ProductoService productos, ClienteService clientes, ReporteService reportes, RestauranteRepository restaurante,
                             InventarioService inventario, UsuarioRepository usuarios, RolRepository roles, PasswordEncoder passwordEncoder) {
        this.productos=productos; this.clientes=clientes; this.reportes=reportes; this.restaurante=restaurante; this.inventario=inventario;
        this.usuarios=usuarios; this.roles=roles; this.passwordEncoder=passwordEncoder;
    }

    @ModelAttribute("portal")
    String portal() { return "PORTAL GERENTE"; }

    @ModelAttribute("menu")
    List<Item> menu() {
        return List.of(new Item("Dashboard", "/gerente"), new Item("Reportes", "/gerente/reportes"),
                new Item("Inventario", "/gerente/inventario"), new Item("Productos", "/gerente/productos"), new Item("Clientes", "/gerente/clientes"),
                new Item("Pedidos y reservas", "/gerente/pedidos"), new Item("Personal", "/gerente/personal"));
    }

    @GetMapping
    String dashboard(Model m) {
        List<Venta> ventasHoy=reportes.filtrar(java.time.LocalDate.now(),java.time.LocalDate.now());
        m.addAttribute("kpis", List.of(new Kpi("VENTAS DE HOY", "S/ "+reportes.total(ventasHoy), ventasHoy.size()+" cobros registrados"),
                new Kpi("PRODUCTOS", String.valueOf(productos.total()), "Catálogo registrado"),
                new Kpi("CLIENTES", String.valueOf(clientes.total()), "Clientes registrados"),
                new Kpi("PEDIDOS PAGADOS", String.valueOf(ventasHoy.size()), "Consulta los reportes para filtrar fechas")));
        m.addAttribute("cabeceras", List.of("Pedido", "Medio de pago", "Fecha", "Total"));
        m.addAttribute("filas", ventasHoy.stream().limit(8).map(v -> fila(null,null,"MC-"+v.getPedidoId(),v.getMedioPago(),String.valueOf(v.getFechaPago()),"S/ "+v.getTotal())).toList());
        m.addAttribute("alertas", List.of());
        m.addAttribute("barras", List.of());
        return "gerente/dashboard";
    }

    @GetMapping("/reportes")
    String reportes(@RequestParam(required=false) @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate desde,
                    @RequestParam(required=false) @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate hasta, Model m) {
        List<Venta> registros=reportes.filtrar(desde,hasta);
        m.addAttribute("desde",desde); m.addAttribute("hasta",hasta);
        return lista(m, "Reportes", "Reportes", "Exportar reporte",
                List.of(new Campo("Desde", desde==null?"":desde.toString()), new Campo("Hasta", hasta==null?"":hasta.toString())),
                List.of(new Kpi("VENTAS", "S/ "+reportes.total(registros), registros.size()+" cobros"), new Kpi("PROMEDIO", "S/ "+(registros.isEmpty()?"0.00":reportes.total(registros).divide(java.math.BigDecimal.valueOf(registros.size()),2,java.math.RoundingMode.HALF_UP)), "Por cobro")),
                null, null,
                List.of("Código de pedido", "Medio de pago", "Fecha", "Total"),
                registros.stream().map(v->fila(null,null,"MC-"+v.getPedidoId(),v.getMedioPago(),String.valueOf(v.getFechaPago()),"S/ "+v.getTotal())).toList());
    }

    @GetMapping("/inventario")
    String inventario(@RequestParam(defaultValue = "Ingredientes") String tab,
                      @RequestParam(required = false) String guardado, Model m) {
        List<Item> tabs = List.of(new Item("Ingredientes", "/gerente/inventario?tab=Ingredientes"),
                new Item("Insumos", "/gerente/inventario?tab=Insumos"),
                new Item("Productos para venta", "/gerente/inventario?tab=Productos para venta"));
        String ayuda = "Vistas disponibles: ingredientes · insumos (envases, bolsas, cubiertos) · productos para venta (stock calculado y disponibilidad).";
        List<String> cab;
        List<Fila> filas;
        String boton = "+ Ingrediente";
        switch (tab) {
            case "Insumos" -> {
                boton = "+ Insumo";
                cab = List.of("Insumo", "Categoría", "Stock", "Mínimo", "Unidad", "Estado");
                filas = List.of();
            }
            case "Productos para venta" -> {
                boton = "+ Producto";
                cab = List.of("Producto", "Categoría", "Stock calculado", "Mínimo", "Unidad", "Estado");
                filas = List.of();
            }
            default -> {
                cab = List.of("Ingrediente", "Categoría", "Stock", "Mínimo", "Unidad", "Estado");
                filas = inventario.listar("Ingrediente");
            }
        }
        if ("Insumos".equals(tab)) filas = inventario.listar("Insumo");
        if ("Productos para venta".equals(tab)) {
            filas = productos.listar().stream().map(p -> fila(null, null, p.getNombre(),
                    p.getCategoria().getNombre(), String.valueOf(p.getStock()), "0", "un.",
                    p.getEstado().equals("DISPONIBLE") ? "Disponible" : "Pausado")).toList();
        }
        m.addAttribute("inventarioGuardado", "1".equals(guardado));
        m.addAttribute("inventarioError", m.asMap().get("error"));
        m.addAttribute("botonUrl", "Productos para venta".equals(tab) ? "/gerente/productos/nuevo"
                : "/gerente/inventario/nuevo?tipo=" + ("Insumos".equals(tab) ? "Insumo" : "Ingrediente"));
        return lista(m, "Inventario", "Inventario", boton, null, null, tabs, ayuda, cab, filas, tab);
    }

    @GetMapping("/productos")
    String productos(@RequestParam(required=false) String q, Model m) {
        m.addAttribute("productos", productos.buscar(q)); m.addAttribute("q",q); m.addAttribute("activo","Productos");
        return "gerente/productos";
    }

    @GetMapping("/pedidos")
    String pedidos(@RequestParam(defaultValue = "Pedidos") String tab, Model m) {
        List<Item> tabs = List.of(new Item("Pedidos", "/gerente/pedidos?tab=Pedidos"), new Item("Reservas", "/gerente/pedidos?tab=Reservas"));
        boolean reservas = "Reservas".equals(tab);
        List<Fila> pedidosReales=restaurante.pedidosRecientes().stream().map(p->fila("Gestionar","/mesero/pedidos/"+p.codigo(),"#"+p.codigo(),p.canal()+" · "+p.cliente(),p.estadoOp(),p.estadoPago(),p.hora(),p.total())).toList();
        List<Fila> reservasReales=restaurante.reservasRecientes().stream().map(r->fila("Ver","/reservas/confirmada?id="+Integer.parseInt(r.codigo().replace("RS-","")),"#"+r.codigo(),r.cliente(),r.estado(),r.fechaHora(),String.valueOf(r.personas()))).toList();
        return lista(m, "Pedidos y reservas", "Pedidos y reservas", "Exportar", null, null, tabs, null,
                reservas ? List.of("Código", "Cliente", "Estado", "Fecha y hora", "Personas", "Acción")
                         : List.of("Código", "Canal / cliente", "Estado operativo", "Estado de pago", "Hora", "Total", "Acción"),
                reservas ? reservasReales
                         : pedidosReales,
                tab);
    }

    @GetMapping("/personal")
    String personal(Model m) {
        m.addAttribute("botonUrl", "/gerente/personal/nuevo");
        List<Fila> colaboradores = usuarios.findByRol_NombreInAndEstado(List.of("MESERO", "COCINA", "CAJERO", "REPARTIDOR"), "ACTIVO")
                .stream().map(u -> fila("Ingresar", "/acceso", u.getNombre() + " " + u.getApellido(),
                        u.getRol().getNombre(), u.getCorreo(), u.getEstado())).toList();
        return lista(m, "Cuentas del personal", "Personal", "+ Crear cuenta", null, null, null,
                "Cada colaborador ingresa en Acceso del personal; la aplicación lo dirige al panel de su rol.",
                List.of("Colaborador", "Rol", "Correo", "Estado", "Acción"), colaboradores);
    }

    // ---- Formularios de alta y edición ----
    @GetMapping("/productos/nuevo")
    String productoNuevo(Model m) {
        m.addAttribute("modo", "Nuevo producto");
        Producto p = new Producto(); p.setEstado("DISPONIBLE"); p.setStock(0);
        m.addAttribute("producto", p); m.addAttribute("categorias", productos.categorias());
        m.addAttribute("activo", "Productos");
        return "gerente/producto-form";
    }

    @GetMapping("/productos/{sku}/editar")
    String productoEditar(@PathVariable String sku, Model m) {
        m.addAttribute("modo", "Editar producto");
        Producto p = productos.obtener(Integer.parseInt(sku)).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        m.addAttribute("producto", p); m.addAttribute("categorias", productos.categorias());
        m.addAttribute("activo", "Productos");
        return "gerente/producto-form";
    }

    @PostMapping("/productos/guardar")
    String productoGuardar(@Valid @ModelAttribute("producto") Producto producto, BindingResult errores,
                           @RequestParam Integer categoriaId, Model m) {
        if (errores.hasErrors()) { m.addAttribute("modo", producto.getId()==null?"Nuevo producto":"Editar producto"); m.addAttribute("categorias", productos.categorias()); m.addAttribute("activo","Productos"); return "gerente/producto-form"; }
        productos.guardar(producto,categoriaId); return "redirect:/gerente/productos";
    }

    @PostMapping("/productos/{id}/eliminar")
    String productoEliminar(@PathVariable Integer id) { productos.eliminar(id); return "redirect:/gerente/productos"; }

    @GetMapping("/clientes")
    String listaClientes(@RequestParam(required=false) String q, @RequestParam(defaultValue="Activos") String estado, Model m) {
        m.addAttribute("clientes", clientes.buscar(q,estado)); m.addAttribute("q",q); m.addAttribute("estado",estado); m.addAttribute("activo","Clientes");
        return "gerente/clientes";
    }

    @GetMapping("/clientes/nuevo")
    String nuevoCliente(Model m) { m.addAttribute("cliente",new com.mandalecarbon.model.Cliente()); m.addAttribute("activo","Clientes"); return "gerente/cliente-form"; }

    @GetMapping("/clientes/{id}/editar")
    String editarCliente(@PathVariable Integer id, Model m) { m.addAttribute("cliente",clientes.obtener(id).orElseThrow()); m.addAttribute("activo","Clientes"); return "gerente/cliente-form"; }

    @PostMapping("/clientes/guardar")
    String guardarCliente(@Valid @ModelAttribute("cliente") com.mandalecarbon.model.Cliente cliente, BindingResult errores, Model m) {
        if(errores.hasErrors()){m.addAttribute("activo","Clientes");return "gerente/cliente-form";}
        clientes.guardar(cliente);return "redirect:/gerente/clientes";
    }

    @PostMapping("/clientes/{id}/estado")
    String cambiarEstadoCliente(@PathVariable Integer id, RedirectAttributes flash){
        clientes.cambiarEstado(id);
        flash.addFlashAttribute("mensaje", "Estado del cliente actualizado. Sus pedidos se conservaron.");
        return "redirect:/gerente/clientes";
    }

    @GetMapping("/inventario/nuevo")
    String itemNuevo(@RequestParam(defaultValue = "Ingrediente") String tipo, Model m) {
        boolean insumo = "Insumo".equals(tipo);
        m.addAttribute("titulo", "Nuevo " + tipo.toLowerCase());
        m.addAttribute("activo", "Inventario");
        m.addAttribute("volver", "/gerente/inventario?tab=" + (insumo ? "Insumos" : "Ingredientes"));
        m.addAttribute("nota", insumo ? "Insumos: envases, bolsas, cubiertos y otros materiales que no son comida."
                : "Ingredientes: lo que se usa en las recetas. Su stock alimenta la disponibilidad de los productos.");
        m.addAttribute("tipo", insumo ? "Insumo" : "Ingrediente");
        return "gerente/form";
    }

    @PostMapping("/inventario/guardar")
    String guardarItemInventario(@RequestParam String nombre, @RequestParam String tipo,
                                 @RequestParam(required = false) String categoria,
                                 @RequestParam String unidad, @RequestParam BigDecimal stock,
                                 @RequestParam BigDecimal stockMinimo,
                                 @RequestParam(required = false) String proveedor,
                                 HttpSession sesion, RedirectAttributes flash) {
        if (!List.of("Ingrediente", "Insumo").contains(tipo)
                || nombre.isBlank() || unidad.isBlank() || stock.signum() < 0
                || stockMinimo.signum() < 0) {
            flash.addFlashAttribute("error", "Revisa los datos: nombre, unidad y stocks deben ser válidos.");
            return "redirect:/gerente/inventario/nuevo?tipo=" + ("Insumo".equals(tipo) ? "Insumo" : "Ingrediente");
        }
        Integer usuarioId = (Integer) sesion.getAttribute("usuarioId");
        inventario.crear(nombre, tipo, categoria, unidad, stock, stockMinimo, proveedor, usuarioId);
        flash.addAttribute("tab", "Insumo".equals(tipo) ? "Insumos" : "Ingredientes");
        flash.addAttribute("guardado", "1");
        return "redirect:/gerente/inventario";
    }

    @GetMapping("/personal/nuevo")
    String colaboradorNuevo(Model m) {
        cargarFormularioPersonal(m);
        return "gerente/personal-form";
    }

    @PostMapping("/personal/guardar")
    String guardarColaborador(@RequestParam String nombre, @RequestParam(required = false) String apellido,
                              @RequestParam String correo, @RequestParam String password,
                              @RequestParam String rol, Model m, RedirectAttributes flash) {
        String correoNormalizado = correo == null ? "" : correo.trim().toLowerCase();
        String rolNormalizado = rol == null ? "" : rol.trim().toUpperCase();
        if (nombre == null || nombre.isBlank() || nombre.trim().length() > 80
                || apellido != null && apellido.trim().length() > 80
                || !correoNormalizado.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || password == null || password.length() < 8
                || !List.of("MESERO", "COCINA", "CAJERO", "REPARTIDOR").contains(rolNormalizado)) {
            m.addAttribute("error", "Completa los datos correctamente. La contraseña debe tener al menos 8 caracteres.");
            m.addAttribute("nombre", nombre); m.addAttribute("apellido", apellido); m.addAttribute("correo", correoNormalizado); m.addAttribute("rol", rolNormalizado);
            cargarFormularioPersonal(m);
            return "gerente/personal-form";
        }
        if (usuarios.existsByCorreo(correoNormalizado)) {
            m.addAttribute("error", "Ya existe una cuenta con ese correo.");
            m.addAttribute("nombre", nombre); m.addAttribute("apellido", apellido); m.addAttribute("correo", correoNormalizado); m.addAttribute("rol", rolNormalizado);
            cargarFormularioPersonal(m);
            return "gerente/personal-form";
        }
        RolEntidad rolEntidad = roles.findByNombre(rolNormalizado)
                .orElseThrow(() -> new IllegalStateException("No existe el rol " + rolNormalizado + " en la base de datos."));
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre.trim()); usuario.setApellido(apellido == null ? "" : apellido.trim());
        usuario.setCorreo(correoNormalizado); usuario.setPasswordHash(passwordEncoder.encode(password)); usuario.setRol(rolEntidad);
        usuarios.save(usuario);
        flash.addFlashAttribute("mensajePersonal", "La cuenta de " + rolNormalizado.toLowerCase() + " se creó correctamente.");
        return "redirect:/gerente/personal";
    }

    private void cargarFormularioPersonal(Model m) {
        m.addAttribute("titulo", "Nueva cuenta del personal");
        m.addAttribute("activo", "Personal");
        m.addAttribute("rolesEquipo", roles.findByNombreIn(List.of("MESERO", "COCINA", "CAJERO", "REPARTIDOR")));
    }

    /** Plantilla común de lista administrativa (reportes, inventario, productos, pedidos, personal). */
    private String lista(Model m, String titulo, String activo, String boton, List<Campo> filtros, List<Kpi> kpis,
                         List<Item> tabs, String ayuda, List<String> cabeceras, List<Fila> filas) {
        return lista(m, titulo, activo, boton, filtros, kpis, tabs, ayuda, cabeceras, filas, null);
    }

    private String lista(Model m, String titulo, String activo, String boton, List<Campo> filtros, List<Kpi> kpis,
                         List<Item> tabs, String ayuda, List<String> cabeceras, List<Fila> filas, String tabActiva) {
        m.addAttribute("titulo", titulo);
        m.addAttribute("activo", activo);
        m.addAttribute("boton", boton);
        m.addAttribute("filtros", filtros);
        m.addAttribute("kpis", kpis);
        m.addAttribute("subtabs", tabs);
        m.addAttribute("ayuda", ayuda);
        m.addAttribute("cabeceras", cabeceras);
        m.addAttribute("filas", filas);
        m.addAttribute("tabActiva", tabActiva);
        return "gerente/lista";
    }
}
