package com.mandalecarbon.repository;
import com.mandalecarbon.model.PedidoEntidad;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PedidoJpaRepository extends JpaRepository<PedidoEntidad,Integer>{List<PedidoEntidad> findAllByOrderByFechaHoraDesc();List<PedidoEntidad> findByEstadoOrderByFechaHoraDesc(String estado);}
