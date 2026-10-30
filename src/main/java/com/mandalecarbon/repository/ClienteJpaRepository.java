package com.mandalecarbon.repository;
import com.mandalecarbon.model.Cliente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ClienteJpaRepository extends JpaRepository<Cliente,Integer>{
 List<Cliente> findByNombreContainingIgnoreCaseOrTelefonoContaining(String nombre,String telefono);
 java.util.Optional<Cliente> findByCorreoIgnoreCase(String correo);
 boolean existsByCorreoIgnoreCase(String correo);
}
