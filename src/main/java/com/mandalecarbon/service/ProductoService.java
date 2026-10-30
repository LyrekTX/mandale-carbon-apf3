package com.mandalecarbon.service;
import com.mandalecarbon.model.CategoriaProducto;
import com.mandalecarbon.model.Producto;
import com.mandalecarbon.repository.CategoriaProductoRepository;
import com.mandalecarbon.repository.ProductoJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestiona el catálogo y las categorías de productos. */
@Service
public class ProductoService {
 private final ProductoJpaRepository productos; private final CategoriaProductoRepository categorias;
 public ProductoService(ProductoJpaRepository p,CategoriaProductoRepository c){productos=p;categorias=c;}
 /** Lista todos los productos, incluidos los pausados. */
 @Transactional(readOnly=true) public List<Producto> listar(){return productos.findAll();}
 /** Lista los productos disponibles para realizar pedidos. */
 @Transactional(readOnly=true) public List<Producto> disponibles(){return productos.findByEstado("DISPONIBLE");}
 /** Busca productos por nombre, ignorando mayúsculas y minúsculas. */
 @Transactional(readOnly=true) public List<Producto> buscar(String q){return q==null||q.isBlank()?listar():productos.findByNombreContainingIgnoreCase(q.trim());}
 /** Busca un producto por identificador. */
 @Transactional(readOnly=true) public Optional<Producto> obtener(Integer id){return productos.findById(id);}
 /** Asocia la categoría existente y guarda el producto. */
 @Transactional public Producto guardar(Producto p,Integer categoriaId){
   CategoriaProducto c=categorias.findById(categoriaId).orElseThrow(()->new IllegalArgumentException("Selecciona una categoría válida"));
   p.setCategoria(c); return productos.save(p);
 }
 /** Pausa el producto para conservar el historial de pedidos. */
 @Transactional public void eliminar(Integer id){Producto p=productos.findById(id).orElseThrow();p.setEstado("PAUSADO");}
 /** Cuenta los productos registrados. */
 @Transactional(readOnly=true) public long total(){return productos.count();}
 /** Lista las categorías del catálogo. */
 @Transactional(readOnly=true) public List<CategoriaProducto> categorias(){return categorias.findAll();}
}
