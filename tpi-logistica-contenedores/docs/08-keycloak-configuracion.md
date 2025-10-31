# Configuración de Keycloak para Autenticación y Autorización

## Introducción

Este documento describe cómo configurar Keycloak como proveedor de identidad federada para el sistema de logística de contenedores. Keycloak proporciona autenticación y autorización mediante tokens JWT.

## Arquitectura de Seguridad

\`\`\`
Cliente/Postman
    ↓
    1. Solicita token a Keycloak
    ↓
Keycloak (Puerto 8180)
    ↓
    2. Retorna JWT token
    ↓
Cliente/Postman
    ↓
    3. Envía petición con token en header Authorization
    ↓
API Gateway (Puerto 8080)
    ↓
    4. Valida token JWT
    ↓
Microservicio específico
    ↓
    5. Valida token y roles
    ↓
    6. Procesa petición
\`\`\`

## Roles del Sistema

El sistema define tres roles principales:

- **CLIENTE**: Puede crear solicitudes y consultar el estado de sus contenedores
- **OPERADOR**: Puede gestionar depósitos, camiones, tarifas y asignar rutas
- **ADMIN**: Tiene acceso completo al sistema
- **TRANSPORTISTA**: Puede ver sus tramos asignados e iniciar/finalizar viajes

## Configuración Inicial de Keycloak

### 1. Acceso a Keycloak

Una vez levantado el sistema con docker-compose, acceder a:

\`\`\`
URL: http://localhost:8180
Usuario: admin
Contraseña: admin123
\`\`\`

### 2. Crear Realm "logistica"

1. En el menú superior izquierdo, hacer clic en el dropdown del realm (dice "master")
2. Hacer clic en "Create Realm"
3. Configurar:
   - **Realm name**: `logistica`
   - **Enabled**: ON
4. Hacer clic en "Create"

### 3. Crear Roles

Ir a **Realm roles** en el menú lateral y crear los siguientes roles:

#### Rol: CLIENTE
1. Clic en "Create role"
2. **Role name**: `CLIENTE`
3. **Description**: `Cliente que solicita traslados de contenedores`
4. Guardar

#### Rol: OPERADOR
1. Clic en "Create role"
2. **Role name**: `OPERADOR`
3. **Description**: `Operador que gestiona el sistema`
4. Guardar

#### Rol: ADMIN
1. Clic en "Create role"
2. **Role name**: `ADMIN`
3. **Description**: `Administrador con acceso completo`
4. Guardar

#### Rol: TRANSPORTISTA
1. Clic en "Create role"
2. **Role name**: `TRANSPORTISTA`
3. **Description**: `Transportista que realiza los traslados`
4. Guardar

### 4. Crear Cliente (Client) para la aplicación

1. Ir a **Clients** en el menú lateral
2. Clic en "Create client"
3. Configurar:
   - **Client type**: `OpenID Connect`
   - **Client ID**: `logistica-backend`
4. Clic en "Next"
5. Configurar capacidades:
   - **Client authentication**: ON
   - **Authorization**: OFF
   - **Authentication flow**: Marcar todas las opciones
6. Clic en "Next"
7. Configurar URLs:
   - **Root URL**: `http://localhost:8080`
   - **Valid redirect URIs**: `http://localhost:8080/*`
   - **Web origins**: `*`
8. Clic en "Save"

### 5. Obtener Client Secret

1. En el cliente recién creado, ir a la pestaña **Credentials**
2. Copiar el **Client secret** (lo necesitaremos para obtener tokens)

### 6. Crear Usuarios de Prueba

#### Usuario Cliente
1. Ir a **Users** en el menú lateral
2. Clic en "Add user"
3. Configurar:
   - **Username**: `cliente1`
   - **Email**: `cliente1@logistica.com`
   - **First name**: `Juan`
   - **Last name**: `Pérez`
   - **Email verified**: ON
4. Guardar
5. Ir a la pestaña **Credentials**
6. Clic en "Set password"
7. Configurar:
   - **Password**: `cliente123`
   - **Temporary**: OFF
8. Guardar
9. Ir a la pestaña **Role mapping**
10. Clic en "Assign role"
11. Buscar y seleccionar el rol `CLIENTE`
12. Clic en "Assign"

#### Usuario Operador
Repetir el proceso con:
- **Username**: `operador1`
- **Email**: `operador1@logistica.com`
- **Password**: `operador123`
- **Rol**: `OPERADOR`

#### Usuario Admin
Repetir el proceso con:
- **Username**: `admin1`
- **Email**: `admin1@logistica.com`
- **Password**: `admin123`
- **Rol**: `ADMIN`

#### Usuario Transportista
Repetir el proceso con:
- **Username**: `transportista1`
- **Email**: `transportista1@logistica.com`
- **Password**: `transportista123`
- **Rol**: `TRANSPORTISTA`

## Obtener Token JWT

### Usando cURL

\`\`\`bash
curl -X POST 'http://localhost:8180/realms/logistica/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=logistica-backend' \
  -d 'client_secret=YOUR_CLIENT_SECRET' \
  -d 'username=cliente1' \
  -d 'password=cliente123' \
  -d 'grant_type=password'
\`\`\`

### Usando Postman

1. Crear nueva petición POST
2. URL: `http://localhost:8180/realms/logistica/protocol/openid-connect/token`
3. En la pestaña **Body**, seleccionar `x-www-form-urlencoded`
4. Agregar los siguientes parámetros:
   - `client_id`: `logistica-backend`
   - `client_secret`: `YOUR_CLIENT_SECRET`
   - `username`: `cliente1`
   - `password`: `cliente123`
   - `grant_type`: `password`
5. Enviar la petición

### Respuesta

\`\`\`json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer"
}
\`\`\`

## Usar el Token en las Peticiones

### En cURL

\`\`\`bash
curl -X GET 'http://localhost:8080/api/clientes' \
  -H 'Authorization: Bearer YOUR_ACCESS_TOKEN'
\`\`\`

### En Postman

1. En la pestaña **Authorization**
2. Seleccionar **Type**: `Bearer Token`
3. Pegar el `access_token` en el campo **Token**

## Validación de Roles por Endpoint

### Clientes Service (Puerto 8081)

| Método | Endpoint | Roles Permitidos |
|--------|----------|------------------|
| POST | /api/clientes | ADMIN, OPERADOR |
| GET | /api/clientes | Todos autenticados |
| GET | /api/clientes/{id} | Todos autenticados |
| PUT | /api/clientes/{id} | ADMIN, OPERADOR |
| DELETE | /api/clientes/{id} | ADMIN |

### Depósitos Service (Puerto 8082)

| Método | Endpoint | Roles Permitidos |
|--------|----------|------------------|
| POST | /api/depositos | ADMIN |
| GET | /api/depositos | ADMIN, OPERADOR |
| PUT | /api/depositos/{id} | ADMIN |
| DELETE | /api/depositos/{id} | ADMIN |

### Camiones Service (Puerto 8083)

| Método | Endpoint | Roles Permitidos |
|--------|----------|------------------|
| POST | /api/camiones | ADMIN |
| GET | /api/camiones | ADMIN, OPERADOR, TRANSPORTISTA |
| PUT | /api/camiones/{id} | ADMIN, OPERADOR |
| DELETE | /api/camiones/{id} | ADMIN |

### Tarifas Service (Puerto 8084)

| Método | Endpoint | Roles Permitidos |
|--------|----------|------------------|
| POST | /api/tarifas | ADMIN |
| GET | /api/tarifas | ADMIN, OPERADOR |
| PUT | /api/tarifas/{id} | ADMIN |
| DELETE | /api/tarifas/{id} | ADMIN |

### Solicitudes Service (Puerto 8085)

| Método | Endpoint | Roles Permitidos |
|--------|----------|------------------|
| POST | /api/solicitudes | CLIENTE, OPERADOR, ADMIN |
| GET | /api/solicitudes | Todos autenticados |
| GET | /api/solicitudes/{id} | Todos autenticados |
| PUT | /api/solicitudes/{id} | OPERADOR, ADMIN |

### Rutas Service (Puerto 8086)

| Método | Endpoint | Roles Permitidos |
|--------|----------|------------------|
| POST | /api/rutas/calcular | OPERADOR, ADMIN |
| POST | /api/rutas/{id}/asignar-camion | OPERADOR, ADMIN |
| POST | /api/tramos/{id}/iniciar | TRANSPORTISTA, ADMIN |
| POST | /api/tramos/{id}/finalizar | TRANSPORTISTA, ADMIN |

## Troubleshooting

### Error: "Invalid token"

- Verificar que el token no haya expirado (duración: 5 minutos por defecto)
- Verificar que el issuer URI sea correcto en la configuración
- Verificar que Keycloak esté accesible desde los microservicios

### Error: "Access Denied" o "Forbidden"

- Verificar que el usuario tenga el rol correcto asignado
- Verificar que el endpoint requiera ese rol específico
- Revisar los logs del microservicio para ver qué roles se están extrayendo del token

### Error: "Unable to connect to Keycloak"

- Verificar que el contenedor de Keycloak esté corriendo: `docker ps`
- Verificar los logs de Keycloak: `docker logs logistica-keycloak`
- Verificar la conectividad de red entre contenedores

## Próximos Pasos

1. Aplicar la misma configuración de seguridad a todos los microservicios
2. Configurar el API Gateway para validar tokens
3. Crear tests de integración con Spring Security Test
4. Implementar refresh token para renovar tokens expirados
