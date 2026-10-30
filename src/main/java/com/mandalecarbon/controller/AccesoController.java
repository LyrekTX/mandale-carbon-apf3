package com.mandalecarbon.controller;

import com.mandalecarbon.model.Acceso;
import com.mandalecarbon.model.Acceso.Rol;
import com.mandalecarbon.model.Usuario;
import com.mandalecarbon.model.Cliente;
import com.mandalecarbon.repository.ClienteJpaRepository;
import com.mandalecarbon.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Acceso del personal (cliente y personal viven en áreas separadas). */
@Controller
public class AccesoController {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final ClienteJpaRepository clientes;
    AccesoController(UsuarioRepository usuarios, PasswordEncoder encoder, ClienteJpaRepository clientes) {
        this.usuarios=usuarios; this.encoder=encoder; this.clientes=clientes;
    }

    @GetMapping("/acceso")
    String acceso(@RequestParam(required = false) String destino, Model m) {
        m.addAttribute("roles", Acceso.ROLES);
        m.addAttribute("destino", destino == null ? "gerente" : destino);
        return "acceso";
    }

    @PostMapping("/acceso")
    String ingresar(@RequestParam String correo, @RequestParam String password, HttpSession sesion, Model m) {
        Usuario u=usuarios.findByCorreoAndEstado(correo.trim(),"ACTIVO").orElse(null);
        if(u==null || !encoder.matches(password,u.getPasswordHash())) {
            m.addAttribute("error","Correo o contraseña incorrectos"); m.addAttribute("roles",Acceso.ROLES); return "acceso";
        }
        String clave=u.getRol().getNombre().toLowerCase();
        Rol r=Acceso.de(clave);
        sesion.setAttribute("rol",clave); sesion.setAttribute("rolEtiqueta",r.etiqueta());
        sesion.setAttribute("nombre",u.getNombre()); sesion.setAttribute("usuarioId",u.getId());
        return "redirect:" + r.inicio();
    }

    @GetMapping("/salir")
    String salir(HttpSession sesion) {
        sesion.invalidate();
        return "redirect:/acceso";
    }

    @PostMapping("/login")
    String ingresarCliente(@RequestParam String correo, @RequestParam String password,
                           HttpSession sesion, org.springframework.web.servlet.mvc.support.RedirectAttributes flash) {
        Cliente cliente = clientes.findByCorreoIgnoreCase(correo.trim()).orElse(null);
        if (cliente == null || !cliente.isActivo() || cliente.getPasswordHash() == null
                || !encoder.matches(password, cliente.getPasswordHash())) {
            flash.addFlashAttribute("errorLogin", "Correo o contraseña incorrectos.");
            return "redirect:/login";
        }
        sesion.setAttribute("clienteId", cliente.getId());
        sesion.setAttribute("clienteNombre", cliente.getNombre());
        sesion.setAttribute("clienteCorreo", cliente.getCorreo());
        sesion.setAttribute("clienteTelefono", cliente.getTelefono());
        return "redirect:/cuenta";
    }

    @PostMapping("/registro")
    String registrarCliente(@RequestParam String nombre, @RequestParam(required = false) String telefono,
                            @RequestParam String correo, @RequestParam String password,
                            @RequestParam String confirmarPassword,
                            HttpSession sesion, Model model) {
        String correoLimpio = correo == null ? "" : correo.trim().toLowerCase();
        if (nombre == null || nombre.isBlank() || nombre.trim().length() > 120
                || !correoLimpio.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            model.addAttribute("errorRegistro", "Ingresa un nombre y un correo válido.");
            model.addAttribute("nombre", nombre); model.addAttribute("correo", correoLimpio); model.addAttribute("telefono", telefono);
            return "cliente/registro";
        }
        if (password == null || password.length() < 8 || !password.equals(confirmarPassword)) {
            model.addAttribute("errorRegistro", "La contraseña debe tener al menos 8 caracteres y ambas deben coincidir.");
            model.addAttribute("nombre", nombre); model.addAttribute("correo", correoLimpio); model.addAttribute("telefono", telefono);
            return "cliente/registro";
        }
        if (telefono != null && !telefono.isBlank() && !telefono.matches("[0-9+() -]{7,20}")) {
            model.addAttribute("errorRegistro", "Revisa el número de teléfono.");
            model.addAttribute("nombre", nombre); model.addAttribute("correo", correoLimpio); model.addAttribute("telefono", telefono);
            return "cliente/registro";
        }
        Cliente cliente = clientes.findByCorreoIgnoreCase(correoLimpio).orElseGet(Cliente::new);
        if (cliente.getPasswordHash() != null) {
            model.addAttribute("errorRegistro", "Ya existe una cuenta con ese correo. Inicia sesión.");
            model.addAttribute("nombre", nombre); model.addAttribute("correo", correoLimpio); model.addAttribute("telefono", telefono);
            return "cliente/registro";
        }
        cliente.setNombre(nombre.trim());
        cliente.setCorreo(correoLimpio);
        cliente.setTelefono(telefono == null || telefono.isBlank() ? null : telefono.trim());
        cliente.setPasswordHash(encoder.encode(password));
        cliente = clientes.save(cliente);
        sesion.setAttribute("clienteId", cliente.getId());
        sesion.setAttribute("clienteNombre", cliente.getNombre());
        sesion.setAttribute("clienteCorreo", cliente.getCorreo());
        sesion.setAttribute("clienteTelefono", cliente.getTelefono());
        return "redirect:/cuenta";
    }

    @org.springframework.web.bind.annotation.PostMapping("/salir-cliente")
    String salirCliente(HttpSession sesion) {
        sesion.removeAttribute("clienteId");
        sesion.removeAttribute("clienteNombre");
        sesion.removeAttribute("clienteCorreo");
        sesion.removeAttribute("clienteTelefono");
        return "redirect:/";
    }
}
