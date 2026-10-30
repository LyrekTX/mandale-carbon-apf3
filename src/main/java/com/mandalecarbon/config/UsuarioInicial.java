package com.mandalecarbon.config;

import com.mandalecarbon.model.RolEntidad;
import com.mandalecarbon.model.Usuario;
import com.mandalecarbon.repository.RolRepository;
import com.mandalecarbon.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UsuarioInicial {
    @Bean
    ApplicationRunner crearGerente(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder encoder,
            @Value("${app.bootstrap.email:}") String email, @Value("${app.bootstrap.password:}") String clave) {
        return args -> {
            if (email.isBlank() || clave.isBlank() || usuarios.existsByCorreo(email))
                return;
            if (clave.length() < 10)
                throw new IllegalStateException("MC_ADMIN_PASSWORD debe tener al menos 10 caracteres");
            RolEntidad rol = roles.findByNombre("GERENTE").orElseThrow(
                    () -> new IllegalStateException("Ejecuta primero el script SQL para crear el rol GERENTE"));
            Usuario u = new Usuario();
            u.setNombre("Administrador");
            u.setApellido("Mandale Carbon");
            u.setCorreo(email);
            u.setPasswordHash(encoder.encode(clave));
            u.setRol(rol);
            usuarios.save(u);
        };
    }
}
