package com.mandalecarbon.repository;
import com.mandalecarbon.model.Venta;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface VentaRepository extends JpaRepository<Venta,Integer>{
 List<Venta> findAllByOrderByFechaPagoDesc();
 List<Venta> findByFechaPagoGreaterThanEqualAndFechaPagoLessThanOrderByFechaPagoDesc(LocalDateTime desde,LocalDateTime hasta);
}
