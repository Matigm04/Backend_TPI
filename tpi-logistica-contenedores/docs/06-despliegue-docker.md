# Guía de Despliegue con Docker

## Requisitos Previos

- Docker 20.10 o superior
- Docker Compose 2.0 o superior
- Al menos 4GB de RAM disponible
- Puertos 8080-8086 y 5432 disponibles

## Estructura del Proyecto

\`\`\`
tpi-logistica/
├── api-gateway/
├── clientes-service/
├── depositos-service/
├── camiones-service/
├── tarifas-service/
├── solicitudes-service/
├── rutas-service/
├── scripts/
│   └── init-databases.sh
├── docker-compose.yml
├── .env.example
└── README.md
\`\`\`

## Configuración Inicial

### 1. Configurar Variables de Entorno

Copiar el archivo de ejemplo y configurar las variables necesarias:

\`\`\`bash
cp .env.example .env
\`\`\`

Editar el archivo `.env` y configurar:
- Credenciales de PostgreSQL
- API Key de Google Maps (opcional para desarrollo)
- Otras configuraciones según necesidad

### 2. Dar Permisos al Script de Inicialización

\`\`\`bash
chmod +x scripts/init-databases.sh
\`\`\`

## Comandos de Despliegue

### Levantar Todo el Sistema

\`\`\`bash
# Construir imágenes y levantar servicios
docker-compose up --build

# O en modo detached (segundo plano)
docker-compose up --build -d
\`\`\`

### Levantar Servicios Específicos

\`\`\`bash
# Solo la base de datos
docker-compose up postgres

# Solo un microservicio específico
docker-compose up clientes-service

# Múltiples servicios
docker-compose up postgres clientes-service depositos-service
\`\`\`

### Ver Logs

\`\`\`bash
# Logs de todos los servicios
docker-compose logs -f

# Logs de un servicio específico
docker-compose logs -f clientes-service

# Últimas 100 líneas
docker-compose logs --tail=100 -f
\`\`\`

### Detener Servicios

\`\`\`bash
# Detener todos los servicios
docker-compose down

# Detener y eliminar volúmenes (CUIDADO: elimina datos)
docker-compose down -v

# Detener y eliminar imágenes
docker-compose down --rmi all
\`\`\`

### Reiniciar Servicios

\`\`\`bash
# Reiniciar todos
docker-compose restart

# Reiniciar uno específico
docker-compose restart clientes-service
\`\`\`

## Verificación del Despliegue

### 1. Verificar Estado de Contenedores

\`\`\`bash
docker-compose ps
\`\`\`

Todos los servicios deben mostrar estado `Up` y `healthy`.

### 2. Verificar Conectividad

\`\`\`bash
# API Gateway
curl http://localhost:8080/actuator/health

# Clientes Service
curl http://localhost:8081/actuator/health

# Depositos Service
curl http://localhost:8082/actuator/health

# Camiones Service
curl http://localhost:8083/actuator/health

# Tarifas Service
curl http://localhost:8084/actuator/health

# Solicitudes Service
curl http://localhost:8085/actuator/health

# Rutas Service
curl http://localhost:8086/actuator/health
\`\`\`

### 3. Verificar Base de Datos

\`\`\`bash
# Conectarse a PostgreSQL
docker exec -it logistica-postgres psql -U logistica

# Listar bases de datos
\l

# Conectarse a una base específica
\c clientes_db

# Listar tablas
\dt
\`\`\`

## Acceso a Servicios

### Endpoints Principales

| Servicio | Puerto | URL Base | Swagger UI |
|----------|--------|----------|------------|
| API Gateway | 8080 | http://localhost:8080 | http://localhost:8080/swagger-ui.html |
| Clientes | 8081 | http://localhost:8081 | http://localhost:8081/swagger-ui.html |
| Depósitos | 8082 | http://localhost:8082 | http://localhost:8082/swagger-ui.html |
| Camiones | 8083 | http://localhost:8083 | http://localhost:8083/swagger-ui.html |
| Tarifas | 8084 | http://localhost:8084 | http://localhost:8084/swagger-ui.html |
| Solicitudes | 8085 | http://localhost:8085 | http://localhost:8085/swagger-ui.html |
| Rutas | 8086 | http://localhost:8086 | http://localhost:8086/swagger-ui.html |

### Acceso a través del API Gateway

Todos los servicios también son accesibles a través del API Gateway:

\`\`\`bash
# Clientes
curl http://localhost:8080/api/clientes

# Depósitos
curl http://localhost:8080/api/depositos

# Camiones
curl http://localhost:8080/api/camiones

# Tarifas
curl http://localhost:8080/api/tarifas

# Solicitudes
curl http://localhost:8080/api/solicitudes

# Rutas
curl http://localhost:8080/api/rutas
\`\`\`

## Troubleshooting

### Problema: Servicios no inician

**Solución:**
\`\`\`bash
# Ver logs detallados
docker-compose logs -f [nombre-servicio]

# Verificar que los puertos no estén ocupados
netstat -tulpn | grep LISTEN

# Limpiar y reconstruir
docker-compose down -v
docker-compose up --build
\`\`\`

### Problema: Base de datos no se conecta

**Solución:**
\`\`\`bash
# Verificar que PostgreSQL esté healthy
docker-compose ps postgres

# Ver logs de PostgreSQL
docker-compose logs postgres

# Reiniciar PostgreSQL
docker-compose restart postgres
\`\`\`

### Problema: Servicios no se comunican entre sí

**Solución:**
\`\`\`bash
# Verificar la red Docker
docker network ls
docker network inspect tpi-logistica_logistica-network

# Verificar DNS interno
docker exec clientes-service ping postgres
docker exec solicitudes-service ping clientes-service
\`\`\`

### Problema: Cambios en código no se reflejan

**Solución:**
\`\`\`bash
# Reconstruir imágenes sin caché
docker-compose build --no-cache [nombre-servicio]
docker-compose up -d [nombre-servicio]
\`\`\`

## Desarrollo Local

### Modo Desarrollo con Hot Reload

Para desarrollo, puedes ejecutar servicios individuales localmente mientras otros corren en Docker:

\`\`\`bash
# Levantar solo PostgreSQL
docker-compose up postgres

# Ejecutar servicio localmente con perfil 'dev'
cd clientes-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
\`\`\`

### Ejecutar Tests

\`\`\`bash
# Tests de un servicio específico
docker-compose run --rm clientes-service ./mvnw test

# O localmente
cd clientes-service
./mvnw test
\`\`\`

## Limpieza y Mantenimiento

### Limpiar Contenedores Detenidos

\`\`\`bash
docker container prune
\`\`\`

### Limpiar Imágenes No Utilizadas

\`\`\`bash
docker image prune -a
\`\`\`

### Limpiar Volúmenes No Utilizados

\`\`\`bash
docker volume prune
\`\`\`

### Limpiar Todo (CUIDADO)

\`\`\`bash
docker system prune -a --volumes
\`\`\`

## Backup de Base de Datos

### Crear Backup

\`\`\`bash
# Backup de todas las bases de datos
docker exec logistica-postgres pg_dumpall -U logistica > backup_$(date +%Y%m%d_%H%M%S).sql

# Backup de una base específica
docker exec logistica-postgres pg_dump -U logistica clientes_db > clientes_backup.sql
\`\`\`

### Restaurar Backup

\`\`\`bash
# Restaurar todas las bases de datos
docker exec -i logistica-postgres psql -U logistica < backup_20250115_120000.sql

# Restaurar una base específica
docker exec -i logistica-postgres psql -U logistica -d clientes_db < clientes_backup.sql
\`\`\`

## Monitoreo

### Ver Uso de Recursos

\`\`\`bash
# Estadísticas en tiempo real
docker stats

# Uso de disco
docker system df
\`\`\`

### Inspeccionar Contenedor

\`\`\`bash
docker inspect clientes-service
\`\`\`

## Próximos Pasos

1. Configurar Keycloak para autenticación
2. Integrar Google Maps API
3. Configurar monitoreo con Prometheus/Grafana
4. Implementar CI/CD pipeline
5. Configurar backups automáticos

## Referencias

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [PostgreSQL Docker Hub](https://hub.docker.com/_/postgres)
