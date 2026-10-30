package com.mandalecarbon.service;

import com.mandalecarbon.model.Datos.Fila;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventarioService {
    private final JdbcTemplate db;

    public InventarioService(JdbcTemplate db) {
        this.db = db;
    }

    @Transactional(readOnly = true)
    public List<Fila> listar(String tipo) {
        return db.query("""
                SELECT nombre, categoria, unidad, stock, stock_minimo
                FROM dbo.item_inventario
                WHERE tipo = ? AND activo = 1
                ORDER BY nombre
                """, (rs, row) -> {
            BigDecimal stock = rs.getBigDecimal("stock");
            BigDecimal minimo = rs.getBigDecimal("stock_minimo");
            String estado = stock.signum() == 0 ? "Agotado"
                    : stock.compareTo(minimo) <= 0 ? "Stock bajo" : "Disponible";
            return new Fila(List.of(rs.getString("nombre"),
                    rs.getString("categoria") == null ? "—" : rs.getString("categoria"),
                    decimal(stock), decimal(minimo), rs.getString("unidad"), estado), null, null);
        }, tipo);
    }

    @Transactional
    public void crear(String nombre, String tipo, String categoria, String unidad,
                      BigDecimal stock, BigDecimal stockMinimo, String proveedor, Integer usuarioId) {
        Integer itemId = db.queryForObject("""
                INSERT INTO dbo.item_inventario(nombre,tipo,categoria,unidad,stock,stock_minimo,proveedor)
                OUTPUT INSERTED.id_item VALUES(?,?,?,?,?,?,?)
                """, Integer.class, nombre.trim(), tipo, categoria, unidad.trim(), stock, stockMinimo, proveedor);

        if (stock.signum() > 0) {
            db.update("""
                    INSERT INTO dbo.movimiento_inventario(id_item,id_usuario,tipo,cantidad,motivo)
                    VALUES(?,?,'ENTRADA',?,'Stock inicial')
                    """, itemId, usuarioId, stock);
        }
    }

    private static String decimal(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}
