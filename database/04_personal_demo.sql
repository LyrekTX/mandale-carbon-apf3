/*
  Cuentas de acceso inicial para el personal de Mandale Carbón.
  Ejecutar después de 01_create_mandale_carbon.sql en SQL Server Management Studio.
  Es idempotente: no duplica usuarios si se ejecuta más de una vez.
  Las contraseñas almacenadas están cifradas con BCrypt.
*/
USE bd_mandale_carbon;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.rol WHERE nombre='MESERO')
  INSERT dbo.rol(nombre,descripcion) VALUES ('MESERO','Atención en salón');
IF NOT EXISTS (SELECT 1 FROM dbo.rol WHERE nombre='COCINA')
  INSERT dbo.rol(nombre,descripcion) VALUES ('COCINA','Preparación de pedidos');
IF NOT EXISTS (SELECT 1 FROM dbo.rol WHERE nombre='CAJERO')
  INSERT dbo.rol(nombre,descripcion) VALUES ('CAJERO','Cobros y comprobantes');
IF NOT EXISTS (SELECT 1 FROM dbo.rol WHERE nombre='REPARTIDOR')
  INSERT dbo.rol(nombre,descripcion) VALUES ('REPARTIDOR','Entrega de pedidos');
GO

INSERT dbo.usuario(nombre,apellido,correo,password_hash,id_rol,estado)
SELECT p.nombre,p.apellido,p.correo,p.password_hash,r.id_rol,'ACTIVO'
FROM (VALUES
  ('Carlos','Mendoza','mesero@mandalecarbon.local','$2a$10$c3s8T19ebRhmPEzAZaCNF.hVisvp9/Km69KcSDdopAlXArwev2hYq','MESERO'),
  ('Rosa','Diaz','cocina@mandalecarbon.local','$2a$10$zkbduZ2aMEEmFynQIdQMe.gBKeoNkUjAlnk3Voe3g12CUu0sn2Ngy','COCINA'),
  ('Maria','Leon','cajero@mandalecarbon.local','$2a$10$w/zUBXcK6yeDj/cq9Y7oCeWczQ4Snret29UMZaCx48VY7dRRKplGe','CAJERO'),
  ('Luis','Ramirez','repartidor@mandalecarbon.local','$2a$10$5IwyzNCx6qzG85ZqlJJiAOwSdFf0jqftBhBmybnPCO6.JJcwDvm86','REPARTIDOR')
) AS p(nombre,apellido,correo,password_hash,rol)
JOIN dbo.rol r ON r.nombre=p.rol
WHERE NOT EXISTS (SELECT 1 FROM dbo.usuario u WHERE u.correo=p.correo);
GO

SELECT u.nombre,u.apellido,u.correo,r.nombre AS rol,u.estado
FROM dbo.usuario u
JOIN dbo.rol r ON r.id_rol=u.id_rol
WHERE u.correo IN ('mesero@mandalecarbon.local','cocina@mandalecarbon.local',
                   'cajero@mandalecarbon.local','repartidor@mandalecarbon.local')
ORDER BY r.nombre;
GO
