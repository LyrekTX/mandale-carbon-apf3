package com.mandalecarbon.repository;
import com.mandalecarbon.model.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductoJpaRepository extends JpaRepository<Producto,Integer>{
 List<Producto> findByNombreContainingIgnoreCase(String q);
 List<Producto> findByEstado(String estado);
}
