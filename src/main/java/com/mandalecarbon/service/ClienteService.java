package com.mandalecarbon.service;
import com.mandalecarbon.model.Cliente;
import com.mandalecarbon.repository.ClienteJpaRepository;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestiona consultas, altas y estado de los clientes del restaurante. */
@Service public class ClienteService{
 private final ClienteJpaRepository repo; public ClienteService(ClienteJpaRepository r){repo=r;}
 /** Devuelve los clientes activos. */
 @Transactional(readOnly=true) public List<Cliente> listar(){return buscar(null,"Activos");}
 /** Filtra clientes por texto y estado; la búsqueda ignora mayúsculas y tildes. */
 @Transactional(readOnly=true) public List<Cliente> buscar(String q,String estado){
  String filtro=normalizar(q);
  return repo.findAll().stream()
   .filter(c -> "Todos".equals(estado) || ("Inactivos".equals(estado) ? !c.isActivo() : c.isActivo()))
   .filter(c -> filtro.isEmpty() || contiene(c.getNombre(),filtro) || contiene(c.getTelefono(),filtro) || contiene(c.getCorreo(),filtro))
   .toList();
 }
 private boolean contiene(String valor,String filtro){return valor!=null&&normalizar(valor).contains(filtro);}
 private String normalizar(String valor){return StringUtils.stripAccents(valor==null?"":valor).toLowerCase(Locale.ROOT).trim();}
 /** Busca un cliente por su identificador. */
 @Transactional(readOnly=true) public Optional<Cliente> obtener(Integer id){return repo.findById(id);}
 /** Guarda los datos de un cliente. */
 @Transactional public Cliente guardar(Cliente c){return repo.save(c);}
 /** Activa o desactiva al cliente indicado; falla si no existe. */
 @Transactional public void cambiarEstado(Integer id){Cliente c=repo.findById(id).orElseThrow();c.setActivo(!c.isActivo());repo.save(c);}
 /** Cuenta los clientes registrados, activos e inactivos. */
 @Transactional(readOnly=true) public long total(){return repo.count();}
}
