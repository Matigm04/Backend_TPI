# Clientes Service

Microservicio para la gestión de clientes en el sistema de logística de contenedores.

## Descripción

Este microservicio es responsable de:
- Registro y gestión de clientes
- Validación de datos de clientes (DNI, email únicos)
- Consulta de clientes por diferentes criterios
- Activación/desactivación de clientes
- Búsqueda de clientes por nombre o apellido

## Tecnologías

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- PostgreSQL / H2
- Lombok
- SpringDoc OpenAPI (Swagger)
- Maven

## Configuración

### Perfiles disponibles

- **dev**: Usa base de datos H2 en memoria (por defecto)
- **prod**: Usa PostgreSQL

### Variables de entorno (perfil prod)

\`\`\`bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/clientesdb
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
\`\`\`

## Ejecución

### Modo desarrollo (H2)

\`\`\`bash
mvn spring-boot:run
\`\`\`

### Modo producción (PostgreSQL)

\`\`\`bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
\`\`\`

### Con Docker

\`\`\`bash
docker build -t clientes-service .
docker run -p 8081:8081 clientes-service
\`\`\`

## Endpoints principales

### Crear cliente
\`\`\`http
POST /api/clientes
Content-Type: application/json

{
  "nombre": "Juan",
  "apellido": "Pérez",
  "dni": "12345678",
  "email": "juan.perez@email.com",
  "telefono": "+54 11 1234-5678",
  "direccion": "Av. Corrientes 1234",
  "ciudad": "Buenos Aires",
  "provincia": "Buenos Aires",
  "codigoPostal": "C1043"
}
\`\`\`

### Obtener cliente por ID
\`\`\`http
GET /api/clientes/{id}
\`\`\`

### Obtener cliente por DNI
\`\`\`http
GET /api/clientes/dni/{dni}
\`\`\`

### Obtener todos los clientes
\`\`\`http
GET /api/clientes
\`\`\`

### Buscar clientes por nombre
\`\`\`http
GET /api/clientes/buscar?nombre=Juan
\`\`\`

### Actualizar cliente
\`\`\`http
PUT /api/clientes/{id}
Content-Type: application/json
\`\`\`

### Desactivar cliente
\`\`\`http
PATCH /api/clientes/{id}/desactivar
\`\`\`

### Eliminar cliente
\`\`\`http
DELETE /api/clientes/{id}
\`\`\`

## Documentación API

Una vez iniciado el servicio, la documentación Swagger está disponible en:

- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/api-docs

## Consola H2 (modo dev)

En modo desarrollo, puedes acceder a la consola H2 en:

http://localhost:8081/h2-console

- JDBC URL: `jdbc:h2:mem:clientesdb`
- Username: `sa`
- Password: (vacío)

## Testing

\`\`\`bash
mvn test
\`\`\`

## Estructura del proyecto

\`\`\`
clientes-service/
├── src/
│   ├── main/
│   │   ├── java/com/logistica/clientes/
│   │   │   ├── config/          # Configuraciones
│   │   │   ├── controller/      # Controladores REST
│   │   │   ├── dto/             # DTOs de request/response
│   │   │   ├── exception/       # Manejo de excepciones
│   │   │   ├── model/           # Entidades JPA
│   │   │   ├── repository/      # Repositorios Spring Data
│   │   │   ├── service/         # Lógica de negocio
│   │   │   └── ClientesServiceApplication.java
│   │   └── resources/
│   │       └── application.yml  # Configuración
│   └── test/                    # Tests
├── Dockerfile
├── pom.xml
└── README.md
\`\`\`

## Notas

- El servicio corre por defecto en el puerto **8081**
- Los clientes tienen un campo `activo` para soft delete
- Se valida unicidad de DNI y email
- Todas las operaciones están documentadas con Swagger
- Se implementa logging con SLF4J
