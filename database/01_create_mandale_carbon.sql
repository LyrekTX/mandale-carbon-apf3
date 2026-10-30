/*
  Mandale Carbon - esquema inicial SQL Server.
  Se puede ejecutar mas de una vez sin borrar datos existentes.
  Requiere permiso CREATE DATABASE la primera vez.
*/
IF DB_ID(N'bd_mandale_carbon') IS NULL
    CREATE DATABASE bd_mandale_carbon;
GO
USE bd_mandale_carbon;
GO

IF OBJECT_ID('dbo.rol','U') IS NULL
CREATE TABLE dbo.rol (
  id_rol INT IDENTITY PRIMARY KEY,
  nombre VARCHAR(30) NOT NULL UNIQUE,
  descripcion VARCHAR(150) NULL
);
GO
IF OBJECT_ID('dbo.usuario','U') IS NULL
CREATE TABLE dbo.usuario (
  id_usuario INT IDENTITY PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL,
  apellido VARCHAR(80) NOT NULL DEFAULT '',
  correo VARCHAR(120) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  id_rol INT NOT NULL REFERENCES dbo.rol(id_rol),
  estado VARCHAR(10) NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','INACTIVO')),
  fecha_creacion DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO
IF OBJECT_ID('dbo.cliente','U') IS NULL
CREATE TABLE dbo.cliente (
  id_cliente INT IDENTITY PRIMARY KEY,
  nombre VARCHAR(120) NOT NULL,
  telefono VARCHAR(20) NULL,
  correo VARCHAR(120) NULL,
  password_hash VARCHAR(255) NULL,
  activo BIT NOT NULL CONSTRAINT DF_cliente_activo DEFAULT 1,
  fecha_creacion DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO
IF COL_LENGTH('dbo.cliente','password_hash') IS NULL
  ALTER TABLE dbo.cliente ADD password_hash VARCHAR(255) NULL;
GO
IF COL_LENGTH('dbo.cliente','activo') IS NULL
  ALTER TABLE dbo.cliente ADD activo BIT NOT NULL CONSTRAINT DF_cliente_activo DEFAULT 1;
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='ix_cliente_correo' AND object_id=OBJECT_ID('dbo.cliente'))
  CREATE INDEX ix_cliente_correo ON dbo.cliente(correo) WHERE correo IS NOT NULL;
GO
IF OBJECT_ID('dbo.direccion_cliente','U') IS NULL
CREATE TABLE dbo.direccion_cliente (
  id_direccion INT IDENTITY PRIMARY KEY,
  id_cliente INT NOT NULL REFERENCES dbo.cliente(id_cliente) ON DELETE CASCADE,
  direccion VARCHAR(250) NOT NULL,
  referencia VARCHAR(200) NULL,
  distrito VARCHAR(80) NOT NULL,
  es_principal BIT NOT NULL DEFAULT 0
);
GO
IF OBJECT_ID('dbo.mesa','U') IS NULL
CREATE TABLE dbo.mesa (
  id_mesa INT IDENTITY PRIMARY KEY,
  numero_mesa INT NOT NULL UNIQUE,
  capacidad INT NOT NULL CHECK (capacidad > 0),
  estado VARCHAR(12) NOT NULL DEFAULT 'DISPONIBLE' CHECK (estado IN ('DISPONIBLE','OCUPADA','RESERVADA'))
);
GO
IF OBJECT_ID('dbo.categoria_producto','U') IS NULL
CREATE TABLE dbo.categoria_producto (
  id_categoria INT IDENTITY PRIMARY KEY,
  nombre VARCHAR(40) NOT NULL UNIQUE,
  descripcion VARCHAR(150) NULL
);
GO
IF OBJECT_ID('dbo.producto','U') IS NULL
CREATE TABLE dbo.producto (
  id_producto INT IDENTITY PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL,
  descripcion VARCHAR(250) NULL,
  precio DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
  stock_disponible INT NOT NULL DEFAULT 0 CHECK (stock_disponible >= 0),
  id_categoria INT NOT NULL REFERENCES dbo.categoria_producto(id_categoria),
  estado VARCHAR(12) NOT NULL DEFAULT 'DISPONIBLE' CHECK (estado IN ('DISPONIBLE','AGOTADO','PAUSADO')),
  fecha_creacion DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO
IF OBJECT_ID('dbo.pedido','U') IS NULL
CREATE TABLE dbo.pedido (
  id_pedido INT IDENTITY PRIMARY KEY,
  fecha_hora DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
  canal VARCHAR(12) NOT NULL CHECK (canal IN ('Delivery','Recojo','Salon')),
  estado VARCHAR(25) NOT NULL DEFAULT 'Por aceptar' CHECK (estado IN ('Por aceptar','En preparación','Listo para recoger','Listo para llevar','En camino','Entregado','Cancelado')),
  estado_pago VARCHAR(15) NOT NULL DEFAULT 'Pendiente' CHECK (estado_pago IN ('Pendiente','Por validar','Pagado','Observado')),
  id_cliente INT NULL REFERENCES dbo.cliente(id_cliente),
  id_mesa INT NULL REFERENCES dbo.mesa(id_mesa),
  id_usuario INT NOT NULL REFERENCES dbo.usuario(id_usuario),
  monto_total DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (monto_total >= 0),
  direccion_entrega VARCHAR(250) NULL,
  referencia VARCHAR(200) NULL,
  observacion VARCHAR(300) NULL
);
GO
IF OBJECT_ID('dbo.detalle_pedido','U') IS NULL
CREATE TABLE dbo.detalle_pedido (
  id_detalle INT IDENTITY PRIMARY KEY,
  id_pedido INT NOT NULL REFERENCES dbo.pedido(id_pedido) ON DELETE CASCADE,
  id_producto INT NOT NULL REFERENCES dbo.producto(id_producto),
  cantidad INT NOT NULL CHECK (cantidad > 0),
  precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario >= 0),
  subtotal DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0),
  observacion VARCHAR(200) NULL
);
GO
IF OBJECT_ID('dbo.venta','U') IS NULL
CREATE TABLE dbo.venta (
  id_venta INT IDENTITY PRIMARY KEY,
  id_pedido INT NOT NULL UNIQUE REFERENCES dbo.pedido(id_pedido),
  tipo_comprobante VARCHAR(10) NOT NULL DEFAULT 'TICKET' CHECK (tipo_comprobante IN ('TICKET','BOLETA')),
  medio_pago VARCHAR(10) NOT NULL CHECK (medio_pago IN ('EFECTIVO','YAPE','PLIN','TARJETA')),
  monto_subtotal DECIMAL(10,2) NOT NULL,
  monto_igv DECIMAL(10,2) NOT NULL DEFAULT 0,
  monto_total DECIMAL(10,2) NOT NULL,
  fecha_pago DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO
IF OBJECT_ID('dbo.reserva','U') IS NULL
CREATE TABLE dbo.reserva (
  id_reserva INT IDENTITY PRIMARY KEY,
  id_cliente INT NOT NULL REFERENCES dbo.cliente(id_cliente),
  id_mesa INT NULL REFERENCES dbo.mesa(id_mesa),
  fecha_hora DATETIME2 NOT NULL,
  cantidad_personas INT NOT NULL CHECK (cantidad_personas > 0),
  estado VARCHAR(15) NOT NULL DEFAULT 'Pendiente' CHECK (estado IN ('Pendiente','Confirmada','Atendida','Cancelada')),
  observacion VARCHAR(250) NULL
);
GO
IF OBJECT_ID('dbo.repartidor','U') IS NULL
CREATE TABLE dbo.repartidor (
  id_repartidor INT IDENTITY PRIMARY KEY,
  id_usuario INT NOT NULL UNIQUE REFERENCES dbo.usuario(id_usuario),
  telefono VARCHAR(20) NULL,
  estado VARCHAR(12) NOT NULL DEFAULT 'DISPONIBLE' CHECK (estado IN ('DISPONIBLE','OCUPADO','INACTIVO'))
);
GO
IF OBJECT_ID('dbo.entrega','U') IS NULL
CREATE TABLE dbo.entrega (
  id_entrega INT IDENTITY PRIMARY KEY,
  id_pedido INT NOT NULL UNIQUE REFERENCES dbo.pedido(id_pedido),
  id_repartidor INT NULL REFERENCES dbo.repartidor(id_repartidor),
  fecha_salida DATETIME2 NULL,
  fecha_entrega DATETIME2 NULL,
  estado VARCHAR(15) NOT NULL DEFAULT 'Pendiente' CHECK (estado IN ('Pendiente','Asignada','En camino','Entregada','Fallida'))
);
GO
IF OBJECT_ID('dbo.item_inventario','U') IS NULL
CREATE TABLE dbo.item_inventario (
  id_item INT IDENTITY PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  tipo VARCHAR(12) NOT NULL CHECK (tipo IN ('Ingrediente','Insumo')),
  categoria VARCHAR(50) NULL,
  unidad VARCHAR(20) NOT NULL,
  stock DECIMAL(12,3) NOT NULL DEFAULT 0 CHECK (stock >= 0),
  stock_minimo DECIMAL(12,3) NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
  proveedor VARCHAR(120) NULL,
  activo BIT NOT NULL DEFAULT 1
);
GO
IF OBJECT_ID('dbo.movimiento_inventario','U') IS NULL
CREATE TABLE dbo.movimiento_inventario (
  id_movimiento INT IDENTITY PRIMARY KEY,
  id_item INT NOT NULL REFERENCES dbo.item_inventario(id_item),
  id_usuario INT NOT NULL REFERENCES dbo.usuario(id_usuario),
  tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('ENTRADA','SALIDA','AJUSTE')),
  cantidad DECIMAL(12,3) NOT NULL CHECK (cantidad > 0),
  motivo VARCHAR(200) NULL,
  fecha DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='ix_pedido_fecha_estado' AND object_id=OBJECT_ID('dbo.pedido'))
  CREATE INDEX ix_pedido_fecha_estado ON dbo.pedido(fecha_hora,estado);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='ix_detalle_pedido' AND object_id=OBJECT_ID('dbo.detalle_pedido'))
  CREATE INDEX ix_detalle_pedido ON dbo.detalle_pedido(id_pedido);
GO

IF NOT EXISTS (SELECT 1 FROM dbo.rol)
  INSERT dbo.rol(nombre,descripcion) VALUES ('GERENTE','Administracion'),('MESERO','Atencion en salon'),('COCINA','Preparacion'),('CAJERO','Cobros'),('REPARTIDOR','Entrega');
IF NOT EXISTS (SELECT 1 FROM dbo.usuario)
  INSERT dbo.usuario(nombre,apellido,correo,password_hash,id_rol)
  SELECT 'Usuario de sistema','Inicial','sistema@mandalecarbon.local',REPLICATE('0',64),id_rol FROM dbo.rol WHERE nombre='GERENTE';
IF NOT EXISTS (SELECT 1 FROM dbo.categoria_producto)
  INSERT dbo.categoria_producto(nombre) VALUES ('Parrillas'),('Combos'),('Bebidas'),('Acompanamientos');
IF NOT EXISTS (SELECT 1 FROM dbo.producto)
  INSERT dbo.producto(nombre,descripcion,precio,stock_disponible,id_categoria)
  SELECT x.nombre,x.descripcion,x.precio,100,c.id_categoria FROM (VALUES
    ('Parrilla clasica','Carne, pollo y chorizo',34.90,'Parrillas'),
    ('Costillas al carbon','Con papa y ensalada',39.90,'Parrillas'),
    ('Combo familiar','Para cuatro personas',89.90,'Combos'),
    ('Pollo a la brasa','Cuarto con papas',29.90,'Parrillas'),
    ('Papas fritas','Porcion mediana',6.00,'Acompanamientos'),
    ('Chicha morada','Vaso 500 ml',6.00,'Bebidas')) x(nombre,descripcion,precio,categoria)
  JOIN dbo.categoria_producto c ON c.nombre=x.categoria;
IF NOT EXISTS (SELECT 1 FROM dbo.mesa)
  INSERT dbo.mesa(numero_mesa,capacidad) VALUES (1,4),(2,4),(3,4),(4,8),(5,2),(6,6),(7,6),(8,4);
GO
