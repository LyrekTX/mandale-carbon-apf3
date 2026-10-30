package com.mandalecarbon.service;
import com.mandalecarbon.model.Venta;
import com.mandalecarbon.repository.VentaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Consulta ventas y calcula agregados para los reportes del gerente. */
@Service public class ReporteService{
 private final VentaRepository repo; public ReporteService(VentaRepository r){repo=r;}
 /** Filtra ventas por fechas inclusivas; cualquiera de los límites puede omitirse. */
 @Transactional(readOnly=true) public List<Venta> filtrar(LocalDate desde,LocalDate hasta){
  if(desde!=null&&hasta!=null)return repo.findByFechaPagoGreaterThanEqualAndFechaPagoLessThanOrderByFechaPagoDesc(desde.atStartOfDay(),hasta.plusDays(1).atStartOfDay());
  return repo.findAllByOrderByFechaPagoDesc().stream().filter(v->desde==null||!v.getFechaPago().toLocalDate().isBefore(desde)).filter(v->hasta==null||!v.getFechaPago().toLocalDate().isAfter(hasta)).toList();
 }
 /** Suma los importes de una lista de ventas. */
 public BigDecimal total(List<Venta> vs){return vs.stream().map(Venta::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add);}
}
