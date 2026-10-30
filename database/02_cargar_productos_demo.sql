/*
  Carga productos de ejemplo para el catálogo público.
  Seguro para volver a ejecutar: conserva productos existentes y agrega solo faltantes.
  Ejecutar después de database/01_create_mandale_carbon.sql.
*/
USE bd_mandale_carbon;
GO

SET XACT_ABORT ON;
BEGIN TRANSACTION;

INSERT INTO dbo.categoria_producto(nombre, descripcion)
SELECT v.nombre, v.descripcion
FROM (VALUES
    ('Parrillas', 'Carnes y platos preparados al carbón'),
    ('Combos', 'Combos para compartir'),
    ('Bebidas', 'Bebidas frías y refrescos'),
    ('Acompanamientos', 'Acompañamientos y porciones')
) AS v(nombre, descripcion)
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.categoria_producto c WHERE c.nombre = v.nombre
);

INSERT INTO dbo.producto(nombre, descripcion, precio, stock_disponible, id_categoria, estado)
SELECT v.nombre, v.descripcion, v.precio, v.stock,
       c.id_categoria, 'DISPONIBLE'
FROM (VALUES
    ('Parrilla clasica', 'Carne, pollo y chorizo a la parrilla.', CAST(34.90 AS DECIMAL(10,2)), 30, 'Parrillas'),
    ('Costillas al carbon', 'Costillas con papas doradas y ensalada.', CAST(39.90 AS DECIMAL(10,2)), 20, 'Parrillas'),
    ('Pollo a la brasa', 'Pollo a la brasa con papas y ensalada.', CAST(29.90 AS DECIMAL(10,2)), 25, 'Parrillas'),
    ('Anticuchos', 'Porcion de anticuchos con papa y choclo.', CAST(22.00 AS DECIMAL(10,2)), 20, 'Parrillas'),
    ('Chorizo parrillero', 'Chorizo dorado al carbon.', CAST(18.00 AS DECIMAL(10,2)), 20, 'Parrillas'),
    ('Combo familiar', 'Parrilla para compartir entre cuatro personas.', CAST(89.90 AS DECIMAL(10,2)), 12, 'Combos'),
    ('Combo personal', 'Pollo, papas y bebida personal.', CAST(24.90 AS DECIMAL(10,2)), 18, 'Combos'),
    ('Chicha morada', 'Chicha morada de la casa, vaso de 500 ml.', CAST(6.00 AS DECIMAL(10,2)), 40, 'Bebidas'),
    ('Limonada', 'Limonada fresca, vaso de 500 ml.', CAST(7.00 AS DECIMAL(10,2)), 35, 'Bebidas'),
    ('Papas fritas', 'Porcion mediana de papas fritas.', CAST(8.00 AS DECIMAL(10,2)), 25, 'Acompanamientos'),
    ('Ensalada fresca', 'Ensalada de temporada.', CAST(7.50 AS DECIMAL(10,2)), 20, 'Acompanamientos')
) AS v(nombre, descripcion, precio, stock, categoria)
JOIN dbo.categoria_producto c ON c.nombre = v.categoria
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.producto p WHERE p.nombre = v.nombre
);

COMMIT TRANSACTION;
GO

-- Verifica los productos que ya deben aparecer en /carta.
SELECT p.id_producto, p.nombre, c.nombre AS categoria, p.precio,
       p.stock_disponible, p.estado
FROM dbo.producto p
JOIN dbo.categoria_producto c ON c.id_categoria = p.id_categoria
WHERE p.estado = 'DISPONIBLE'
ORDER BY c.nombre, p.nombre;
GO
