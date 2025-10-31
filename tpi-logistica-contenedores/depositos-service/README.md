# Depósitos Service

Microservicio para la gestión de depósitos de contenedores en el sistema de logística.

## Características

- CRUD completo de depósitos
- Búsqueda de depósitos cercanos por geolocalización (fórmula de Haversine)
- Validación de datos de entrada
- Manejo de excepciones centralizado
- Documentación con Swagger/OpenAPI
- Soporte para H2 (desarrollo) y PostgreSQL (producción)

## Tecnologías

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- PostgreSQL / H2
- Lombok
- Swagger/OpenAPI
- Maven

## Endpoints Principales

### POST /api/depositos
Crear un nuevo depósito

### GET /api/depositos
Listar todos los depósitos (con filtro opcional de activos)

### GET /api/depositos/{id}
Obtener un depósito por ID

### PUT /api/depositos/{id}
Actualizar un depósito existente

### DELETE /api/depositos/{id}
Eliminar (desactivar) un depósito

### GET /api/depositos/cercanos
Buscar depósitos cercanos a una ubicación
- Parámetros: latitud, longitud, radioKm

## Ejecución

### Modo desarrollo (H2)
\`\`\`bash
mvn spring-boot:run
\`\`\`

### Modo producción (PostgreSQL)
\`\`\`bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
\`\`\`

### Docker
\`\`\`bash
docker build -t depositos-service .
docker run -p 8082:8082 depositos-service
\`\`\`

## Documentación API

Una vez iniciado el servicio, acceder a:
- Swagger UI: http://localhost:8082/swagger-ui.html
- OpenAPI JSON: http://localhost:8082/api-docs

## Consola H2 (solo en desarrollo)

http://localhost:8082/h2-console
- JDBC URL: jdbc:h2:mem:depositosdb
- Usuario: sa
- Password: (vacío)
