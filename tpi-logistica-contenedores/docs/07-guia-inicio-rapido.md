# Guía de Inicio Rápido

Esta guía te ayudará a levantar el sistema completo en menos de 5 minutos.

## Prerrequisitos

✅ Docker instalado  
✅ Docker Compose instalado  
✅ Puertos 8080-8086 y 5432 disponibles

## Pasos

### 1. Clonar/Descargar el Proyecto

\`\`\`bash
cd tpi-logistica
\`\`\`

### 2. Configurar Variables de Entorno (Opcional)

\`\`\`bash
cp .env.example .env
# Editar .env si es necesario (opcional para desarrollo)
\`\`\`

### 3. Dar Permisos al Script

\`\`\`bash
chmod +x scripts/init-databases.sh
\`\`\`

### 4. Levantar el Sistema

\`\`\`bash
docker-compose up --build -d
\`\`\`

Este comando:
- Construye todas las imágenes Docker
- Crea la red interna
- Levanta PostgreSQL con 6 bases de datos
- Levanta los 6 microservicios
- Levanta el API Gateway

### 5. Esperar a que Todo Esté Listo

\`\`\`bash
# Ver el progreso
docker-compose logs -f

# O verificar el estado
docker-compose ps
\`\`\`

Espera hasta que todos los servicios muestren estado `healthy` (aproximadamente 1-2 minutos).

### 6. Verificar que Todo Funciona

\`\`\`bash
# Verificar API Gateway
curl http://localhost:8080/actuator/health

# Verificar un servicio específico
curl http://localhost:8081/api/clientes
\`\`\`

### 7. Acceder a Swagger UI

Abre tu navegador en:
- **API Gateway**: http://localhost:8080/swagger-ui.html
- **Clientes Service**: http://localhost:8081/swagger-ui.html
- **Otros servicios**: Puertos 8082-8086

## Prueba Rápida

### Crear un Cliente

\`\`\`bash
curl -X POST http://localhost:8080/api/clientes \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellido": "Pérez",
    "email": "juan.perez@example.com",
    "telefono": "+54 11 1234-5678",
    "direccion": "Av. Corrientes 1234, CABA"
  }'
\`\`\`

### Listar Clientes

\`\`\`bash
curl http://localhost:8080/api/clientes
\`\`\`

## Detener el Sistema

\`\`\`bash
docker-compose down
\`\`\`

## Problemas Comunes

### Puerto ya en uso

\`\`\`bash
# Ver qué está usando el puerto
lsof -i :8080

# Cambiar el puerto en docker-compose.yml
\`\`\`

### Servicios no inician

\`\`\`bash
# Ver logs detallados
docker-compose logs -f [nombre-servicio]

# Reiniciar todo
docker-compose down -v
docker-compose up --build
\`\`\`

## Siguiente Paso

Lee la [Guía de Despliegue Completa](06-despliegue-docker.md) para más detalles sobre configuración, troubleshooting y operaciones avanzadas.
