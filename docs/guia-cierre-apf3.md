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

1. **GitHub:** el repositorio privado ya está publicado y contiene dos commits. Falta documentar los hitos solicitados por el curso y acompañarlos con evidencias; el historial actual no demuestra por sí mismo una entrega histórica al 40 %.
2. **Avances al 40%:** registrar los hitos que solicita el curso como commits separados y documentarlos con fechas/descripciones. El commit inicial actual no demuestra por sí solo esa secuencia histórica.
3. **TDD:** las pruebas iniciales eran de regresión añadidas después de implementar las funciones. La nueva búsqueda de clientes sin tildes sí siguió un ciclo TDD: la prueba falló primero y pasó tras integrar Apache Commons Lang. Aplicar el mismo proceso en los siguientes cambios.
4. **Librería de apoyo:** integrado Apache Commons Lang para que la búsqueda de clientes no distinga tildes. El uso está descrito en el README y cubierto por una prueba unitaria.
5. **Javadoc:** completado para los paquetes, servicios, controladores, modelos y repositorios. Se genera con `docs/generar-javadoc.ps1` y queda en `docs/javadoc/index.html`.
6. **Sustentación:** preparar diapositivas de hasta 10 minutos, probar la demostración y distribuir la exposición entre los integrantes, si es grupal.
7. **Evidencias:** agregar capturas del sistema, GitHub, pruebas, arquitectura y taller de versionamiento cuando estén disponibles. No declarar asistencia al taller ni participación que no se haya confirmado.

## Guion breve para la demostración

1. Presentar el problema del restaurante y el alcance del avance.
2. Mostrar el diagrama MVC/DAO y explicar el flujo navegador → controlador → servicio → repositorio → SQL Server.
3. Recorrer un flujo funcional: catálogo, pedido, cocina/caja y persistencia.
4. Explicar validación, roles y BCrypt como medidas de seguridad implementadas.
5. Mostrar las pruebas disponibles, el historial Git/GitHub y qué parte de TDD se aplicó realmente.
6. Cerrar con los límites y siguientes mejoras, sin presentar funcionalidades pendientes como terminadas.
