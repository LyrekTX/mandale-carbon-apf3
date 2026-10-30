# Guía de cierre del APF3

## Qué ya está preparado

- Aplicación Java 21 con Spring Boot y vistas Thymeleaf.
- Separación MVC entre controladores y vistas; servicios con reglas de negocio; repositorios como DAO.
- Persistencia en SQL Server y contraseñas protegidas con BCrypt.
- Funcionalidades operativas de catálogo, clientes, pedidos, cobros, reservas, inventario básico y roles.
- Pruebas de regresión de servicios: 11 casos ejecutados correctamente el 30/10/2026.
- Diagrama y explicación de arquitectura en `docs/arquitectura.md`.
- Repositorio Git local inicializado en la rama `main`, con commit inicial.
- Informe consolidado APF1/APF2/APF3 en la carpeta `outputs` del espacio de trabajo.

## Qué falta para cumplir la consigna

1. **GitHub:** crear un repositorio privado para el código y publicar el historial. Hace falta iniciar sesión con la cuenta GitHub del estudiante en el navegador. No crear un repositorio público por defecto porque el historial Git contiene el nombre y correo configurados localmente.
2. **Avances al 40%:** registrar los hitos que solicita el curso como commits separados y documentarlos con fechas/descripciones. El commit inicial actual no demuestra por sí solo esa secuencia histórica.
3. **TDD:** las pruebas actuales son de regresión añadidas después de implementar las funciones. En las siguientes mejoras, crear primero una prueba fallida, implementar el comportamiento y luego comprobar que pase; describir honestamente la diferencia en la sustentación.
4. **Librería de apoyo:** la consigna ofrece Guava, Apache POI, Apache Commons y Logback como ejemplos. Verificar qué dependencia se usará realmente y vincularla a una necesidad concreta del sistema antes de afirmar que se cumple este punto.
5. **Javadoc:** documentar clases y métodos públicos principales, generar la documentación y enlazarla desde el informe.
6. **Sustentación:** preparar diapositivas de hasta 10 minutos, probar la demostración y distribuir la exposición entre los integrantes, si es grupal.
7. **Evidencias:** agregar capturas del sistema, GitHub, pruebas, arquitectura y taller de versionamiento cuando estén disponibles. No declarar asistencia al taller ni participación que no se haya confirmado.

## Guion breve para la demostración

1. Presentar el problema del restaurante y el alcance del avance.
2. Mostrar el diagrama MVC/DAO y explicar el flujo navegador → controlador → servicio → repositorio → SQL Server.
3. Recorrer un flujo funcional: catálogo, pedido, cocina/caja y persistencia.
4. Explicar validación, roles y BCrypt como medidas de seguridad implementadas.
5. Mostrar las pruebas disponibles, el historial Git/GitHub y qué parte de TDD se aplicó realmente.
6. Cerrar con los límites y siguientes mejoras, sin presentar funcionalidades pendientes como terminadas.
