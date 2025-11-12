# Guía Completa para Probar la Aplicación

## Índice
1. [Requisitos Previos](#requisitos-previos)
2. [Configuración Inicial](#configuración-inicial)
3. [Iniciar la Aplicación](#iniciar-la-aplicación)
4. [Configurar Keycloak](#configurar-keycloak)
5. [Probar los Endpoints](#probar-los-endpoints)
6. [Escenarios de Prueba](#escenarios-de-prueba)
7. [Troubleshooting](#troubleshooting)

---

## 1. Requisitos Previos

### Software Necesario
- **Docker Desktop** instalado y corriendo
- **Docker Compose** (incluido en Docker Desktop)
- **Postman** o **Thunder Client** (extensión de VS Code) para probar APIs
- **Git** para clonar el repositorio
- Navegador web moderno

### Verificar Instalación
\`\`\`bash
docker --version
docker-compose --version
\`\`\`

---

## 2. Configuración Inicial

### Paso 1: Configurar Variables de Entorno

1. **Copiar el archivo de ejemplo:**
\`\`\`bash
cp .env.example .env
\`\`\`

2. **Editar el archivo `.env`** y completar los valores:

\`\`\`env
# Base de datos PostgreSQL
POSTGRES_USER=logistica_user
POSTGRES_PASSWORD=logistica_pass_2025
POSTGRES_DB=logistica_db

# Keycloak
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin123
KEYCLOAK_DB_PASSWORD=keycloak_pass_2025

# Google Maps API (IMPORTANTE: Debes obtener tu propia API Key)
GOOGLE_MAPS_API_KEY=TU_API_KEY_AQUI

# JWT Configuration
JWT_ISSUER_URI=http://localhost:9090/realms/logistica-realm
\`\`\`

### Paso 2: Obtener Google Maps API Key

**IMPORTANTE:** Necesitas una API Key de Google Maps para que funcione el cálculo de distancias.

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. Habilita la API "Distance Matrix API"
4. Ve a "Credenciales" → "Crear credenciales" → "Clave de API"
5. Copia la API Key generada
6. Pégala en el archivo `.env` en `GOOGLE_MAPS_API_KEY`

**Alternativa para pruebas sin Google Maps:**
Si no quieres configurar Google Maps ahora, el sistema usará el cálculo de Haversine (aproximado) como fallback. Puedes dejar la variable vacía:
\`\`\`env
GOOGLE_MAPS_API_KEY=
\`\`\`

### Paso 3: Verificar Puertos Disponibles

Asegúrate de que estos puertos estén libres:
- **8080** - API Gateway
- **8081** - Clientes Service
- **8082** - Depósitos Service
- **8083** - Camiones Service
- **8084** - Tarifas Service
- **8085** - Solicitudes Service
- **8086** - Rutas Service
- **5432** - PostgreSQL
- **9090** - Keycloak

\`\`\`bash
# En Windows (PowerShell)
netstat -ano | findstr "8080"

# En Linux/Mac
lsof -i :8080
\`\`\`

---

## 3. Iniciar la Aplicación

### Paso 1: Construir las Imágenes Docker

\`\`\`bash
# Construir todas las imágenes (primera vez o después de cambios en el código)
docker-compose build
(debo tener abierto la aplicacion de docker abierta en la pc para que el comando permita construir las imagenes)

# Esto puede tardar 5-10 minutos la primera vez
\`\`\`

### Paso 2: Levantar los Servicios

\`\`\`bash
# Levantar todos los servicios
docker-compose up -d

# Ver los logs en tiempo real
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f api-gateway
\`\`\`

### Paso 3: Verificar que Todo Esté Corriendo

\`\`\`bash
# Ver el estado de los contenedores
docker-compose ps

# Deberías ver todos los servicios en estado "Up"
\`\`\`

**Espera 2-3 minutos** para que todos los servicios inicien completamente.

### Paso 4: Verificar Conectividad

Abre tu navegador y verifica:

- **API Gateway Health:** http://localhost:8080/actuator/health
- **Keycloak Admin:** http://localhost:8180 (usuario: `admin`, password: `admin123`)
- **Swagger Clientes:** http://localhost:8081/swagger-ui/index.html

---

## 4. Configurar Keycloak

### Paso 1: Acceder a Keycloak Admin Console

1. Abre http://localhost:8180
2. Click en "Administration Console"
3. Login: `admin` / `admin123`

### Paso 2: Crear el Realm

1. En el menú superior izquierdo, click en el dropdown "master"
2. Click en "Create Realm"
3. Nombre: `logistica-realm`
4. Click "Create"

### Paso 3: Crear los Roles

1. En el menú lateral, click en "Realm roles"
2. Click "Create role"
3. Crear estos 3 roles:

**Rol 1: CLIENTE**
- Name: `CLIENTE`
- Description: `Cliente que solicita traslados`
- Click "Save"

**Rol 2: OPERADOR**
- Name: `OPERADOR`
- Description: `Operador que gestiona el sistema`
- Click "Save"

**Rol 3: TRANSPORTISTA**
- Name: `TRANSPORTISTA`
- Description: `Transportista que realiza los traslados`
- Click "Save"

### Paso 4: Crear un Cliente (Client) para la API

1. En el menú lateral, click en "Clients"
2. Click "Create client"
3. Configuración:
   - **Client ID:** `logistica-api`
   - **Client authentication:** ON
   - **Authorization:** OFF
   - **Valid redirect URIs:** `*`
   - **Web origins:** `*`
4. Click "Save"
5. Ve a la pestaña "Credentials"
6. Copia el **Client Secret** (lo necesitarás para obtener tokens)

### Paso 5: Crear Usuarios de Prueba

**Usuario 1: Cliente**
1. Click en "Users" → "Add user"
2. Username: `cliente1`
3. Email: `cliente1@test.com`
4. First name: `Matias`
5. Last name: `Gimenez`
6. Email verified: ON
7. Click "Create"
8. Ve a la pestaña "Credentials"
9. Set password: `cliente123`
10. Temporary: OFF
11. Click "Set password"
12. Ve a la pestaña "Role mapping"
13. Click "Assign role"
14. Selecciona `CLIENTE`
15. Click "Assign"

**Usuario 2: Operador**
1. Repite el proceso con:
   - Username: `operador1`
   - Email: `operador1@test.com`
   - Password: `operador123`
   - Rol: `OPERADOR`

**Usuario 3: Transportista**
1. Repite el proceso con:
   - Username: `transportista1`
   - Email: `transportista1@test.com`
   - Password: `transportista123`
   - Rol: `TRANSPORTISTA`

---

## 5. Probar los Endpoints

### Paso 1: Obtener un Token JWT

**Usando cURL:**
\`\`\`bash
curl -X POST http://localhost:9090/realms/logistica-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=logistica-api" \
  -d "client_secret=TU_CLIENT_SECRET_AQUI" \
  -d "username=operador1" \
  -d "password=operador123" \
  -d "grant_type=password"
\`\`\`

**Usando Postman:**
1. Crea una nueva request POST
2. URL: `http://localhost:8180/realms/logistica-realm/protocol/openid-connect/token`
3. Body → x-www-form-urlencoded:
   - `client_id`: `logistica-api`
   - `client_secret`: (el que copiaste de Keycloak)
   - `username`: `operador1`
   - `password`: `operador123`
   - `grant_type`: `password`
4. Send

**Respuesta:**
\`\`\`json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "expires_in": 300,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI...",
  "token_type": "Bearer"
}
\`\`\`

**Copia el `access_token`** - lo usarás en todas las peticiones.

### Paso 2: Probar Endpoints Básicos

#### 1. Listar Clientes (requiere rol OPERADOR)

\`\`\`bash
curl -X GET http://localhost:8080/api/clientes \
  -H "Authorization: Bearer TU_TOKEN_AQUI"
\`\`\`

curl -X GET http://localhost:8080/api/clientes \
  -H "Authorization: 

**Postman:**
- Method: GET
- URL: `http://localhost:8080/api/clientes`
- Headers:
  - `Authorization`: `Bearer TU_TOKEN_AQUI`

**Respuesta esperada:** Lista de clientes precargados

#### 2. Crear un Cliente (requiere rol OPERADOR)

\`\`\`bash
curl -X POST http://localhost:8080/api/clientes \
  -H "Authorization: Bearer TU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "María",
    "apellido": "González",
    "email": "maria.gonzalez@email.com",
    "telefono": "+54 11 5555-6666",
    "documentoTipo": "DNI",
    "documentoNumero": "40123456",
    "direccion": "Av. Corrientes 1234, CABA"
  }'
\`\`\`

#### 3. Listar Depósitos

\`\`\`bash
curl -X GET http://localhost:8080/api/depositos \
  -H "Authorization: Bearer TU_TOKEN_AQUI"
\`\`\`

#### 4. Listar Camiones Disponibles

\`\`\`bash
curl -X GET http://localhost:8080/api/camiones/disponibles \
  -H "Authorization: Bearer TU_TOKEN_AQUI"
\`\`\`

#### 5. Crear una Solicitud de Traslado

**Postman:**
- **Method:** POST
- **URL:** `http://localhost:8080/api/solicitudes`
- **Headers:**
  - `Authorization`: `Bearer TU_TOKEN_AQUI`
  - `Content-Type`: `application/json`
- **Body (raw JSON):**
\`\`\`json
{
  "clienteId": 1,
  "contenedor": {
    "identificacion": "CONT-TEST-001",
    "peso": 5000,
    "volumen": 15,
    "direccionOrigen": "Av. Corrientes 1000, CABA",
    "latitudOrigen": -34.6037,
    "longitudOrigen": -58.3816,
    "direccionDestino": "Av. Libertador 5000, Vicente López",
    "latitudDestino": -34.5430,
    "longitudDestino": -58.4632
  }
}
\`\`\`

**Nota:** Todos los campos del contenedor son obligatorios, incluyendo las coordenadas de origen y destino.

---

## 6. Escenarios de Prueba

### Escenario 1: Flujo Completo de Solicitud

**Objetivo:** Crear una solicitud, calcular ruta, asignar camión y completar el traslado.

---

#### **Paso 1: Crear Solicitud**

**Postman:**
- **Method:** POST
- **URL:** `http://localhost:8080/api/solicitudes`
- **Headers:**
  - `Authorization`: `Bearer <tu-token-operador>`
  - `Content-Type`: `application/json`
- **Body (raw JSON):**
\`\`\`json
{
  "clienteId": 1,
  "contenedor": {
    "identificacion": "CONT-TEST-001",
    "peso": 5000,
    "volumen": 15,
    "direccionOrigen": "Juan de garay 1755, Córdoba",
    "latitudOrigen": -31.4173,
    "longitudOrigen": -64.1834,
    "direccionDestino": "De los toscanos 6581, Córdoba",
    "latitudDestino": -31.3707,
    "longitudDestino": -64.2478
  }
}
\`\`\`

**⚠️ IMPORTANTE:** 
- Todos los campos son **obligatorios**
- `peso` y `volumen` deben ser números decimales mayores a 0
- Las coordenadas deben estar en rangos válidos:
  - Latitud: entre -90 y 90
  - Longitud: entre -180 y 180
- Para obtener coordenadas de una dirección, usá [Google Maps](https://maps.google.com) → click derecho en el mapa → "¿Qué hay aquí?"

**Ejemplos de Coordenadas en Argentina:**
- **Buenos Aires (Obelisco):** Lat: -34.6037, Long: -58.3816
- **Córdoba (Centro):** Lat: -31.4173, Long: -64.1834
- **Rosario (Monumento):** Lat: -32.9468, Long: -60.6393

**Respuesta esperada (201 Created):**
\`\`\`json
{
  "id": 1,
  "numero": "SOL-2025-00001",
  "estado": "PENDIENTE",
  "clienteId": 1,
  "contenedor": {
    "id": 1,
    "identificacion": "CONT-TEST-001",
    "peso": 5000,
    "volumen": 15,
    "direccionOrigen": "Juan de garay 1755, Córdoba",
    "latitudOrigen": -31.4173,
    "longitudOrigen": -64.1834,
    "direccionDestino": "De los toscanos 6581, Córdoba",
    "latitudDestino": -31.3707,
    "longitudDestino": -64.2478
  },
  "fechaCreacion": "2025-11-12T00:15:30.123456"
}
\`\`\`

**📝 Nota:** Guardá el `id` de la solicitud para los siguientes pasos.

---

#### **Paso 2: Calcular Ruta Tentativa**

**Postman:**
- **Method:** POST
- **URL:** `http://localhost:8080/api/rutas/calcular`
- **Headers:**
  - `Authorization`: `Bearer <tu-token-operador>`
  - `Content-Type`: `application/json`
- **Body (raw JSON):**
\`\`\`json
{
  "solicitudId": 1
}
\`\`\`

**Respuesta esperada (201 Created):**
\`\`\`json
{
  "id": 1,
  "solicitudId": 1,
  "cantidadTramos": 1,
  "cantidadDepositos": 0,
  "distanciaTotalKm": 10.31,
  "costoEstimado": 1031.0000,
  "tiempoEstimadoHoras": 1,
  "activa": true,
  "tramos": [
    {
      "id": 1,
      "orden": 1,
      "origenTipo": "ORIGEN",
      "origenDireccion": "Av. Corrientes 1000, CABA",
      "destinoTipo": "DESTINO",
      "destinoDireccion": "Av. Libertador 5000, Vicente López",
      "tipoTramo": "ORIGEN_DESTINO",
      "estado": "ESTIMADO",
      "distanciaKm": 10.31,
      "costoAproximado": 1031.0000,
      "camionId": null
    }
  ],
  "fechaCreacion": "2025-11-12T00:26:25.159099722"
}
\`\`\`

**📝 Nota:** Guardá el `tramos[0].id` (ID del tramo) para el siguiente paso.

---

#### **Paso 3: Asignar Camión al Tramo**

**Postman:**
- **Method:** POST
- **URL:** `http://localhost:8080/api/rutas/tramos/1/asignar-camion`
  - ⚠️ **Reemplazá `1` con el ID del tramo del paso anterior**
- **Headers:**
  - `Authorization`: `Bearer <tu-token-operador>`
  - `Content-Type`: `application/json`
- **Body (raw JSON):**
\`\`\`json
{
  "camionId": 1
}
\`\`\`

**Respuesta esperada (200 OK):**
\`\`\`json
{
  "id": 1,
  "solicitudId": 1,
  "tramos": [
    {
      "id": 1,
      "estado": "ASIGNADO",
      "camionId": 1,
      ...
    }
  ]
}
\`\`\`

**📝 Nota:** El estado del tramo cambió de `ESTIMADO` a `ASIGNADO` y ahora tiene un `camionId`.

---

#### **Paso 4: Iniciar Tramo (como Transportista)**

**⚠️ IMPORTANTE:** Debes obtener un token del usuario `transportista1` para este paso.

**Postman - Obtener Token Transportista:**
- **Method:** POST
- **URL:** `http://localhost:8180/realms/logistica-realm/protocol/openid-connect/token`
- **Headers:**
  - `Content-Type`: `application/x-www-form-urlencoded`
- **Body (x-www-form-urlencoded):**
  - `client_id`: `logistica-api`
  - `client_secret`: `<tu-client-secret>`
  - `username`: `transportista1`
  - `password`: `transportista123`
  - `grant_type`: `password`

**Postman - Iniciar Tramo:**
- **Method:** POST
- **URL:** `http://localhost:8080/api/rutas/tramos/1/iniciar`
  - ⚠️ **Reemplazá `1` con el ID del tramo**
- **Headers:**
  - `Authorization`: `Bearer <token-transportista>`

**Respuesta esperada (200 OK):**
\`\`\`json
{
  "id": 1,
  "estado": "EN_CURSO",
  "fechaHoraInicio": "2025-11-12T01:30:00",
  "camionId": 1,
  ...
}
\`\`\`

**📝 Nota:** El estado cambió de `ASIGNADO` a `EN_CURSO` y se registró `fechaHoraInicio`.

---

#### **Paso 5: Finalizar Tramo**

**Postman:**
- **Method:** POST
- **URL:** `http://localhost:8080/api/rutas/tramos/1/finalizar`
  - ⚠️ **Reemplazá `1` con el ID del tramo**
- **Headers:**
  - `Authorization`: `Bearer <token-transportista>`

**Respuesta esperada (200 OK):**
\`\`\`json
{
  "id": 1,
  "estado": "COMPLETADO",
  "fechaHoraInicio": "2025-11-12T01:30:00",
  "fechaHoraFin": "2025-11-12T02:45:00",
  "costoReal": 1031.0000,
  "camionId": 1,
  ...
}
\`\`\`

**📝 Nota:** El estado cambió de `EN_CURSO` a `COMPLETADO` y se registró `fechaHoraFin`.

---

#### **Paso 6: Verificar Estado de la Solicitud**

**Postman:**
- **Method:** GET
- **URL:** `http://localhost:8080/api/solicitudes/1`
  - ⚠️ **Reemplazá `1` con el ID de tu solicitud**
- **Headers:**
  - `Authorization`: `Bearer <tu-token>`

**Respuesta esperada (200 OK):**
\`\`\`json
{
  "id": 1,
  "numero": "SOL-2025-00001",
  "estado": "COMPLETADA",
  ...
}
\`\`\`

**📝 Nota:** El estado de la solicitud debería haber cambiado a `COMPLETADA` automáticamente.

---

#### **📊 Resumen del Flujo:**

| Paso | Endpoint | Estado Inicial | Estado Final | Usuario |
|------|----------|----------------|--------------|---------|
| 1 | POST /solicitudes | - | PENDIENTE | Operador |
| 2 | POST /rutas/calcular | PENDIENTE | PENDIENTE (ruta creada) | Operador |
| 3 | POST /tramos/{id}/asignar-camion | ESTIMADO | ASIGNADO | Operador |
| 4 | POST /tramos/{id}/iniciar | ASIGNADO | EN_CURSO | Transportista |
| 5 | POST /tramos/{id}/finalizar | EN_CURSO | COMPLETADO | Transportista |
| 6 | GET /solicitudes/{id} | PENDIENTE | COMPLETADA | Cualquiera |

### Escenario 2: Búsqueda de Depósitos Cercanos

\`\`\`bash
GET http://localhost:8080/api/depositos/cercanos?latitud=-34.6037&longitud=-58.3816&radio=50
\`\`\`

### Escenario 3: Consultar Tarifas Vigentes

\`\`\`bash
GET http://localhost:8080/api/tarifas/vigentes
\`\`\`

---

## 7. Troubleshooting

### Problema: "Connection refused" al acceder a los endpoints

**Solución:**
\`\`\`bash
# Verificar que los contenedores estén corriendo
docker-compose ps

# Ver logs de errores
docker-compose logs api-gateway
docker-compose logs clientes-service

# Reiniciar servicios
docker-compose restart
\`\`\`

### Problema: "Unauthorized" o "403 Forbidden"

**Causas comunes:**
1. Token JWT expirado (duran 5 minutos)
2. Rol incorrecto para el endpoint
3. Token no incluido en el header

**Solución:**
- Obtén un nuevo token
- Verifica que el usuario tenga el rol correcto
- Asegúrate de incluir `Authorization: Bearer TOKEN`

### Problema: Google Maps API no funciona

**Solución:**
1. Verifica que la API Key sea válida
2. Verifica que la API "Distance Matrix API" esté habilitada en Google Cloud
3. Revisa los logs del rutas-service:
\`\`\`bash
docker-compose logs rutas-service | grep "Google Maps"
\`\`\`

### Problema: Base de datos no tiene datos

**Solución:**
\`\`\`bash
# Reiniciar servicios para que se ejecuten los scripts data-test.sql
docker-compose down
docker-compose up -d

# O ejecutar manualmente los scripts
docker exec -it postgres psql -U logistica_user -d clientes_db -f /docker-entrypoint-initdb.d/data-test.sql
\`\`\`

### Problema: Keycloak no inicia

**Solución:**
\`\`\`bash
# Ver logs de Keycloak
docker-compose logs keycloak

# Reiniciar solo Keycloak
docker-compose restart keycloak

# Esperar 1-2 minutos para que inicie completamente
\`\`\`

### Comandos Útiles

\`\`\`bash
# Ver todos los logs
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f api-gateway

# Reiniciar un servicio específico
docker-compose restart clientes-service

# Detener todos los servicios
docker-compose down

# Detener y eliminar volúmenes (CUIDADO: borra datos)
docker-compose down -v

# Reconstruir un servicio específico
docker-compose build clientes-service
docker-compose up -d clientes-service

# Ver uso de recursos
docker stats

# Acceder a la consola de un contenedor
docker exec -it clientes-service bash

# Ver logs de PostgreSQL
docker-compose logs postgres
\`\`\`

---

## Resumen de URLs Importantes

| Servicio | URL | Descripción |
|----------|-----|-------------|
| API Gateway | http://localhost:8080 | Punto de entrada principal |
| Keycloak Admin | http://localhost:8180 | Administración de usuarios |
| Swagger Clientes | http://localhost:8081/swagger-ui/index.html | Documentación API Clientes |
| Swagger Depósitos | http://localhost:8082/swagger-ui/index.html | Documentación API Depósitos |
| Swagger Camiones | http://localhost:8083/swagger-ui/index.html | Documentación API Camiones |
| Swagger Tarifas | http://localhost:8084/swagger-ui/index.html | Documentación API Tarifas |
| Swagger Solicitudes | http://localhost:8085/swagger-ui/index.html | Documentación API Solicitudes |
| Swagger Rutas | http://localhost:8086/swagger-ui/index.html | Documentación API Rutas |

---

## Checklist de Configuración

- [ ] Docker Desktop instalado y corriendo
- [ ] Archivo `.env` configurado
- [ ] Google Maps API Key obtenida (opcional)
- [ ] Puertos 8080-8086, 5432, 9090 disponibles
- [ ] `docker-compose build` ejecutado exitosamente
- [ ] `docker-compose up -d` ejecutado
- [ ] Todos los servicios en estado "Up"
- [ ] Keycloak accesible en http://localhost:9090
- [ ] Realm "logistica-realm" creado
- [ ] Roles CLIENTE, OPERADOR, TRANSPORTISTA creados
- [ ] Cliente "logistica-api" creado
- [ ] Usuarios de prueba creados
- [ ] Token JWT obtenido exitosamente
- [ ] Endpoint de prueba funcionando

---

## Próximos Pasos

Una vez que tengas todo funcionando:

1. **Explorar Swagger UI** de cada microservicio para ver todos los endpoints disponibles
2. **Probar los escenarios completos** descritos en la sección 6
3. **Revisar los logs** para entender el flujo de datos entre microservicios
4. **Crear tu propia colección de Postman** con todos los endpoints
5. **Experimentar con diferentes roles** y verificar las restricciones de seguridad

---

¡Éxito con las pruebas! 🚀
