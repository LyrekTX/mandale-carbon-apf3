package com.mandalecarbon.repository;
import com.mandalecarbon.model.Usuario;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UsuarioRepository extends JpaRepository<Usuario,Integer>{
 @EntityGraph(attributePaths = "rol")
 Optional<Usuario> findByCorreoAndEstado(String correo,String estado);
 boolean existsByCorreo(String correo);
 @EntityGraph(attributePaths = "rol")
 List<Usuario> findByRol_NombreInAndEstado(List<String> roles, String estado);
}
