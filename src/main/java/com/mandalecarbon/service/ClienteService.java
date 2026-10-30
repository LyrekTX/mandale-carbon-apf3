package com.mandalecarbon.service;
import com.mandalecarbon.model.Cliente;
import com.mandalecarbon.repository.ClienteJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service public class ClienteService{
 private final ClienteJpaRepository repo; public ClienteService(ClienteJpaRepository r){repo=r;}
 @Transactional(readOnly=true) public List<Cliente> listar(){return buscar(null,"Activos");}
 @Transactional(readOnly=true) public List<Cliente> buscar(String q,String estado){
  String filtro=q==null?"":q.trim().toLowerCase();
  return repo.findAll().stream()
   .filter(c -> "Todos".equals(estado) || ("Inactivos".equals(estado) ? !c.isActivo() : c.isActivo()))
   .filter(c -> filtro.isEmpty() || contiene(c.getNombre(),filtro) || contiene(c.getTelefono(),filtro) || contiene(c.getCorreo(),filtro))
   .toList();
 }
 private boolean contiene(String valor,String filtro){return valor!=null&&valor.toLowerCase().contains(filtro);}
 @Transactional(readOnly=true) public Optional<Cliente> obtener(Integer id){return repo.findById(id);}
 @Transactional public Cliente guardar(Cliente c){return repo.save(c);}
 @Transactional public void cambiarEstado(Integer id){Cliente c=repo.findById(id).orElseThrow();c.setActivo(!c.isActivo());repo.save(c);}
 @Transactional(readOnly=true) public long total(){return repo.count();}
}
