/* Agrega estado activo/inactivo a clientes sin borrar ni modificar sus pedidos. */
IF COL_LENGTH('dbo.cliente', 'activo') IS NULL
BEGIN
  ALTER TABLE dbo.cliente
    ADD activo BIT NOT NULL
      CONSTRAINT DF_cliente_activo DEFAULT 1;
END;
GO

/* Verificación: los clientes existentes quedan activos inicialmente. */
SELECT id_cliente, nombre, activo
FROM dbo.cliente
ORDER BY nombre;
GO
