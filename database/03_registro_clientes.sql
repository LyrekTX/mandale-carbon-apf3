/* Ejecutar una sola vez en SQL Server para habilitar las cuentas de clientes. */
USE bd_mandale_carbon;
GO
IF COL_LENGTH('dbo.cliente','password_hash') IS NULL
  ALTER TABLE dbo.cliente ADD password_hash VARCHAR(255) NULL;
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='ix_cliente_correo' AND object_id=OBJECT_ID('dbo.cliente'))
  CREATE INDEX ix_cliente_correo ON dbo.cliente(correo) WHERE correo IS NOT NULL;
GO
