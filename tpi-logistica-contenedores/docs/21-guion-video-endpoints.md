# Guion para Video - Explicación de Endpoints

**Duración estimada:** 4-5 minutos  
**Sección del video:** Recursos y Endpoints por Microservicio

---

## Introducción (30 segundos)

"Ahora vamos a explicar cómo organizamos los recursos y endpoints de nuestra API REST. Todos los endpoints están accesibles a través del API Gateway en el puerto 8080, y cada microservicio expone sus propios recursos de forma independiente."

---

## 1. Servicio de Clientes (30 segundos)

**[Mostrar diagrama o pantalla del servicio]**

"Empezamos con el **servicio de clientes**, que gestiona el registro y datos de los clientes. Los endpoints principales son:

- **POST /api/clientes** - Para registrar nuevos clientes con sus datos personales
- **GET /api/clientes/{id}** - Para consultar información de un cliente específico
- **PUT /api/clientes/{id}** - Para actualizar datos como teléfono o dirección

Este servicio es accesible por clientes, operadores y administradores, con restricciones según el rol."

---

## 2. Servicio de Solicitudes (45 segundos)

**[Mostrar diagrama o pantalla del servicio]**

"El **servicio de solicitudes** es el corazón del sistema. Aquí los clientes crean sus pedidos de traslado de contenedores. Los endpoints clave son:

- **POST /api/solicitudes** - Crea una nueva solicitud incluyendo los datos del contenedor, ubicación de origen y destino. El sistema genera automáticamente un número de solicitud único.
- **GET /api/solicitudes/{id}** - Permite consultar el estado de una solicitud
- **PUT /api/solicitudes/{id}/estado** - Los operadores pueden cambiar el estado de la solicitud: de BORRADOR a PROGRAMADA, luego EN_TRANSITO y finalmente ENTREGADA
- **GET /api/solicitudes/pendientes** - Muestra todos los contenedores pendientes de entrega con su ubicación actual

Este servicio se comunica con el servicio de clientes para validar que el cliente existe."

---

## 3. Servicio de Rutas (45 segundos)

**[Mostrar diagrama o pantalla del servicio]**

"El **servicio de rutas** es donde ocurre la planificación logística. Sus endpoints principales son:

- **POST /api/rutas/calcular** - Este es muy importante: calcula múltiples opciones de ruta para una solicitud. Puede generar rutas directas o con depósitos intermedios, mostrando distancia, costo estimado y tiempo para cada opción.
- **POST /api/rutas** - Una vez que el operador elige la mejor opción, este endpoint crea la ruta definitiva con todos sus tramos
- **GET /api/rutas/solicitud/{id}** - Permite consultar la ruta asignada a una solicitud

Este servicio se integra con Google Maps API para calcular distancias reales entre ubicaciones."

---

## 4. Servicio de Depósitos (30 segundos)

**[Mostrar diagrama o pantalla del servicio]**

"El **servicio de depósitos** gestiona los puntos intermedios de almacenamiento. Los endpoints son:

- **POST /api/depositos** - Registra nuevos depósitos con su ubicación, capacidad y costo de estadía diario
- **GET /api/depositos** - Lista todos los depósitos disponibles
- **GET /api/depositos/{id}/contenedores** - Muestra qué contenedores están actualmente en un depósito específico, útil para asignar el siguiente tramo de transporte"

---

## 5. Servicio de Camiones (45 segundos)

**[Mostrar diagrama o pantalla del servicio]**

"El **servicio de camiones** gestiona la flota y los transportistas. Los endpoints más importantes son:

- **POST /api/camiones** - Registra camiones con su capacidad de peso, volumen, consumo de combustible y costo por kilómetro
- **GET /api/camiones/disponibles** - Busca camiones disponibles que cumplan con los requisitos de peso y volumen del contenedor
- **POST /api/camiones/{id}/asignar-tramo** - Asigna un camión específico a un tramo de la ruta
- **POST /api/camiones/{id}/iniciar-tramo** y **finalizar-tramo** - Estos endpoints son usados por los transportistas para registrar el inicio y fin de cada viaje

También tenemos endpoints para gestionar transportistas y consultar sus tramos asignados."

---

## 6. Servicio de Tarifas (30 segundos)

**[Mostrar diagrama o pantalla del servicio]**

"El **servicio de tarifas** centraliza toda la configuración de costos. Los endpoints principales son:

- **POST /api/tarifas** - Crea tarifas para diferentes conceptos: costo por kilómetro, precio del combustible, cargo de gestión
- **GET /api/tarifas/vigentes** - Obtiene las tarifas activas en una fecha específica
- **POST /api/tarifas/calcular-estimado** - Calcula el costo estimado de un traslado antes de asignar la ruta
- **POST /api/tarifas/calcular-real** - Calcula el costo real una vez finalizado el traslado, usando datos reales de consumo y distancia"

---

## 7. Servicio de Seguimiento (30 segundos)

**[Mostrar diagrama o pantalla del servicio]**

"Finalmente, el **servicio de seguimiento** permite rastrear los contenedores en tiempo real:

- **POST /api/seguimiento/registrar** - Registra cada cambio de estado del contenedor
- **GET /api/seguimiento/contenedor/{id}** - Muestra el historial completo de movimientos de un contenedor
- **GET /api/seguimiento/contenedor/{id}/ubicacion-actual** - Devuelve la ubicación actual del contenedor, el tramo en el que está y el transportista asignado

Este servicio es fundamental para que los clientes puedan hacer seguimiento de sus envíos."

---

## Seguridad y Roles (30 segundos)

**[Mostrar tabla de roles]**

"Todos los endpoints están protegidos con autenticación JWT mediante Keycloak. Implementamos tres roles:

- **ROLE_CLIENTE** - Puede crear solicitudes y consultar solo sus propios recursos
- **ROLE_OPERADOR** - Tiene acceso completo para gestionar solicitudes, rutas, depósitos y asignaciones
- **ROLE_TRANSPORTISTA** - Puede ver sus tramos asignados y registrar inicio/fin de viajes
- **ROLE_ADMIN** - Acceso total al sistema

Cada endpoint valida el token JWT y verifica que el usuario tenga el rol adecuado antes de procesar la solicitud."

---

## Conclusión (20 segundos)

"En resumen, nuestra API REST está organizada en 6 microservicios independientes, cada uno con su propia base de datos y responsabilidad clara. Todos los endpoints siguen convenciones REST estándar, usan formato JSON, y están completamente documentados con Swagger/OpenAPI para facilitar su uso y testing."

---

## Tips para la presentación:

1. **Mostrar Swagger UI** mientras explicas cada servicio
2. **Destacar la comunicación entre servicios** (ej: solicitudes llama a clientes, rutas llama a Google Maps)
3. **Mencionar códigos HTTP** brevemente (200, 201, 404, etc.)
4. **Enfatizar la seguridad** con JWT y roles
5. **Tener ejemplos de request/response** listos por si preguntan

---

## Preguntas frecuentes que pueden surgir:

**P: ¿Por qué separaron en tantos microservicios?**  
R: "Cada microservicio tiene una responsabilidad única y puede escalar independientemente. Por ejemplo, el servicio de seguimiento puede recibir muchas consultas, mientras que el de tarifas se usa menos frecuentemente."

**P: ¿Cómo se comunican los microservicios entre sí?**  
R: "Usamos RestTemplate para comunicación síncrona HTTP. Por ejemplo, cuando se crea una solicitud, el servicio de solicitudes valida que el cliente existe llamando al servicio de clientes."

**P: ¿Qué pasa si un servicio falla?**  
R: "Implementamos manejo de errores con try-catch y devolvemos códigos HTTP apropiados. En producción, agregaríamos circuit breakers con Resilience4j para mayor resiliencia."

**P: ¿Cómo documentaron la API?**  
R: "Usamos Swagger/OpenAPI con anotaciones en los controllers. Cada microservicio expone su propia documentación en /swagger-ui.html, y están todas accesibles a través del API Gateway."
