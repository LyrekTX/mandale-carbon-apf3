package com.mandalecarbon.repository;
import com.mandalecarbon.model.RolEntidad;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RolRepository extends JpaRepository<RolEntidad,Integer>{
 Optional<RolEntidad> findByNombre(String nombre);
 java.util.List<RolEntidad> findByNombreIn(java.util.List<String> nombres);
}
