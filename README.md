# Mandale Carbón · APF3

Aplicación web Java 21, Spring Boot, Thymeleaf y SQL Server para gestionar catálogo, clientes, pedidos y cobros. Toma como referencia la separación Controller / Service / Repository / Entity del proyecto `demo.zip`, adaptada al restaurante y al esquema `bd_mandale_carbon`.

## Alcance funcional implementado

- CRUD de productos con categoría, precio, stock, estado y validación. “Eliminar” pausa el producto para conservar el historial de pedidos.
- CRUD de clientes con validación de nombre, correo y teléfono.
- Catálogo web leído de SQL Server.
- Registro de pedidos por el mesero, detalles de producto y cambio de estado.
- Registro de cobros y comprobantes.
- Dashboard y reportes de ventas leídos desde la base, con filtro por fechas.
- Inicio de sesión del personal validado con BCrypt; el gerente puede abrir todos los paneles y cada otro rol accede a su área.
- Registro e inicio de sesión de clientes con contraseña BCrypt, vinculando pedidos de clientes registrados a su cuenta.
- Alta de cuentas de personal por rol (mesero, cocina, cajero y repartidor) desde el panel del gerente.
- Selector de tema claro/nocturno guardado en el navegador.
- Entidades y repositorios JPA/Spring Data para productos, categorías, clientes, pedidos y sus detalles, roles, usuarios y ventas. El registro de cobros sigue usando `JdbcTemplate` con transacción SQL.

Las reservas, el inventario básico de ingredientes e insumos y el reparto están conectados a SQL Server. La gestión de recetas, la facturación electrónica y los pagos electrónicos no forman parte de esta versión.

## Arquitectura

La interfaz Thymeleaf sigue MVC con controladores Spring MVC. Los servicios concentran reglas de negocio y los repositorios actúan como DAO: Spring Data JPA para entidades y `JdbcTemplate` para consultas operativas. El diagrama y el detalle de responsabilidades están en `docs/arquitectura.md`.

## Base de datos

Ejecuta `database/01_create_mandale_carbon.sql` en SQL Server Management Studio. El script crea la base, las relaciones, roles, categorías, mesas y productos de ejemplo, e incluye el campo de contraseña de clientes. Si la base ya existía antes de activar el registro, ejecuta también `database/03_registro_clientes.sql` para agregar ese campo sin borrar datos.

Configura estas variables en tu entorno:

- `MC_DB_URL`, por ejemplo `jdbc:sqlserver://localhost:1433;databaseName=bd_mandale_carbon;encrypt=true;trustServerCertificate=true`
- `MC_DB_USER`
- `MC_DB_PASSWORD`
- `MC_ADMIN_EMAIL`
- `MC_ADMIN_PASSWORD` (mínimo 10 caracteres)

Al iniciar, si el correo administrador no existe, la app lo crea con rol GERENTE y almacena la contraseña con BCrypt.

## Ejecutar localmente

Con Java 21, Maven y SQL Server disponibles, define las variables anteriores y ejecuta:

```powershell
mvn spring-boot:run
```

Abre `http://localhost:8080/acceso` e ingresa con `MC_ADMIN_EMAIL` y `MC_ADMIN_PASSWORD`.

## Pruebas automatizadas

Ejecuta `mvn test` para correr las pruebas unitarias. Las pruebas de servicios usan Mockito y no requieren una conexión a SQL Server. Cubren la activación y desactivación de clientes, los filtros de la lista y las validaciones iniciales de pedidos para los canales configurados.

Las funcionalidades se desarrollaron antes de añadir estas pruebas de regresión; para demostrar TDD en los siguientes cambios, primero escribe una prueba que falle, implementa la regla y vuelve a ejecutar la suite.

## Ejecutar en contenedores

1. Copia `.env.example` como `.env` y cambia las tres contraseñas por valores propios.
2. Inicia solo SQL Server: `docker compose up -d sqlserver`.
3. Espera a que el contenedor esté saludable y ejecuta el script SQL dentro de `database/01_create_mandale_carbon.sql` desde SSMS conectado a `localhost,1433`.
4. Inicia la aplicación: `docker compose up -d --build app`.
5. Abre `http://localhost:8080/acceso`.

La base usa el volumen `mandale-sql-data`. Conserva el archivo `.env` privado.

## Estructura

- `src/main/java/.../controller`: rutas web y formularios.
- `src/main/java/.../service`: reglas de negocio y validación.
- `src/main/java/.../repository`: Spring Data JPA y acceso JDBC para pedidos/cobros.
- `src/main/java/.../model`: entidades persistentes y modelos de interfaz.
- `src/main/resources/templates`: vistas Thymeleaf existentes y módulos de gestión.
- `database`: creación de esquema SQL Server.
- `Dockerfile`, `docker-compose.yml`: empaquetado y ejecución local en contenedores.

Para esta entrega APF3, la consigna pide evidenciar MVC, TDD, DAO, SOLID, seguridad, al menos una librería de apoyo, Git/GitHub, avances versionados y una sustentación. La ejecución con Docker es una opción local adicional; no sustituye la publicación ni las evidencias de GitHub.

