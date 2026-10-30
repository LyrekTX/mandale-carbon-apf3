package com.mandalecarbon.service;
import com.mandalecarbon.model.CategoriaProducto;
import com.mandalecarbon.model.Producto;
import com.mandalecarbon.repository.CategoriaProductoRepository;
import com.mandalecarbon.repository.ProductoJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {
 private final ProductoJpaRepository productos; private final CategoriaProductoRepository categorias;
 public ProductoService(ProductoJpaRepository p,CategoriaProductoRepository c){productos=p;categorias=c;}
 @Transactional(readOnly=true) public List<Producto> listar(){return productos.findAll();}
 @Transactional(readOnly=true) public List<Producto> disponibles(){return productos.findByEstado("DISPONIBLE");}
 @Transactional(readOnly=true) public List<Producto> buscar(String q){return q==null||q.isBlank()?listar():productos.findByNombreContainingIgnoreCase(q.trim());}
 @Transactional(readOnly=true) public Optional<Producto> obtener(Integer id){return productos.findById(id);}
 @Transactional public Producto guardar(Producto p,Integer categoriaId){
   CategoriaProducto c=categorias.findById(categoriaId).orElseThrow(()->new IllegalArgumentException("Selecciona una categoría válida"));
   p.setCategoria(c); return productos.save(p);
 }
 @Transactional public void eliminar(Integer id){Producto p=productos.findById(id).orElseThrow();p.setEstado("PAUSADO");}
 @Transactional(readOnly=true) public long total(){return productos.count();}
 @Transactional(readOnly=true) public List<CategoriaProducto> categorias(){return categorias.findAll();}
}
