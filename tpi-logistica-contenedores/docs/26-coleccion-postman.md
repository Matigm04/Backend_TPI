# Colección de Postman para Testing

## Importar Colección

Puedes crear una colección de Postman con estos endpoints para facilitar las pruebas.

## Configuración de Variables de Entorno en Postman

1. Crea un nuevo Environment llamado "Logistica Local"
2. Agrega estas variables:

| Variable | Initial Value | Current Value |
|----------|---------------|---------------|
| base_url | http://localhost:8080 | http://localhost:8080 |
| keycloak_url | http://localhost:9090 | http://localhost:9090 |
| client_id | logistica-api | logistica-api |
| client_secret | (tu client secret) | (tu client secret) |
| username | operador1 | operador1 |
| password | operador123 | operador123 |
| access_token | | (se llenará automáticamente) |

## Estructura de la Colección

\`\`\`
Logistica TPI
├── 0. Auth
│   └── Get Token (POST)
├── 1. Clientes
│   ├── Listar Clientes (GET)
│   ├── Crear Cliente (POST)
│   ├── Obtener Cliente (GET)
│   ├── Actualizar Cliente (PUT)
│   └── Eliminar Cliente (DELETE)
├── 2. Depósitos
│   ├── Listar Depósitos (GET)
│   ├── Crear Depósito (POST)
│   ├── Buscar Cercanos (GET)
│   └── Actualizar Depósito (PUT)
├── 3. Camiones
│   ├── Listar Camiones (GET)
│   ├── Crear Camión (POST)
│   ├── Listar Disponibles (GET)
│   ├── Asignar Camión (PUT)
│   └── Liberar Camión (PUT)
├── 4. Tarifas
│   ├── Listar Tarifas (GET)
│   ├── Crear Tarifa (POST)
│   ├── Tarifas Vigentes (GET)
│   └── Actualizar Tarifa (PUT)
├── 5. Solicitudes
│   ├── Crear Solicitud (POST)
│   ├── Listar Solicitudes (GET)
│   ├── Obtener Solicitud (GET)
│   ├── Seguimiento (GET)
│   └── Filtrar por Estado (GET)
└── 6. Rutas
    ├── Calcular Rutas Tentativas (GET)
    ├── Asignar Ruta (POST)
    ├── Asignar Camión a Tramo (POST)
    ├── Iniciar Tramo (POST)
    ├── Finalizar Tramo (POST)
    └── Obtener Ruta (GET)
\`\`\`

## Request: Get Token

**Pre-request Script:**
\`\`\`javascript
// No script needed
\`\`\`

**Request:**
- Method: POST
- URL: `{{keycloak_url}}/realms/logistica-realm/protocol/openid-connect/token`
- Body (x-www-form-urlencoded):
  - client_id: `{{client_id}}`
  - client_secret: `{{client_secret}}`
  - username: `{{username}}`
  - password: `{{password}}`
  - grant_type: `password`

**Tests Script:**
\`\`\`javascript
// Guardar el token en la variable de entorno
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("access_token", jsonData.access_token);
    console.log("Token obtenido y guardado");
} else {
    console.log("Error al obtener token");
}
\`\`\`

## Configuración Global de Headers

Para todos los requests (excepto Get Token), agrega en la pestaña "Authorization":
- Type: Bearer Token
- Token: `{{access_token}}`

O en Headers:
- Key: `Authorization`
- Value: `Bearer {{access_token}}`

## Ejemplos de Requests

### 1. Crear Cliente

\`\`\`
POST {{base_url}}/api/clientes
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
  "nombre": "Carlos",
  "apellido": "Rodríguez",
  "email": "carlos.rodriguez@email.com",
  "telefono": "+54 11 4444-5555",
  "documentoTipo": "DNI",
  "documentoNumero": "35987654",
  "direccion": "Av. Santa Fe 2000, CABA"
}
\`\`\`

### 2. Crear Solicitud

\`\`\`
POST {{base_url}}/api/solicitudes
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
  "clienteId": 1,
  "contenedor": {
    "codigoIdentificacion": "CONT-{{$randomInt}}",
    "pesoKg": 5000,
    "volumenM3": 15,
    "largoM": 3,
    "anchoM": 2.5,
    "altoM": 2,
    "descripcion": "Contenedor para construcción"
  },
  "ubicacionOrigen": "Av. Corrientes 1000, CABA",
  "ubicacionDestino": "Av. Libertador 5000, Vicente López",
  "observaciones": "Entrega urgente"
}
\`\`\`

### 3. Calcular Rutas Tentativas

\`\`\`
GET {{base_url}}/api/rutas/solicitud/1/tentativas
Authorization: Bearer {{access_token}}
\`\`\`

### 4. Buscar Depósitos Cercanos

\`\`\`
GET {{base_url}}/api/depositos/cercanos?latitud=-34.6037&longitud=-58.3816&radio=50
Authorization: Bearer {{access_token}}
\`\`\`

## Tests Automatizados

Puedes agregar tests en cada request para validar las respuestas:

\`\`\`javascript
// Test para verificar status 200
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

// Test para verificar que la respuesta es JSON
pm.test("Response is JSON", function () {
    pm.response.to.be.json;
});

// Test para verificar estructura de respuesta
pm.test("Response has id", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('id');
});

// Test para verificar tiempo de respuesta
pm.test("Response time is less than 2000ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(2000);
});
\`\`\`

## Flujo de Prueba Completo

1. **Get Token** - Obtener token JWT
2. **Listar Clientes** - Verificar datos precargados
3. **Crear Solicitud** - Crear nueva solicitud
4. **Calcular Rutas** - Obtener rutas tentativas
5. **Asignar Ruta** - Asignar ruta a la solicitud
6. **Listar Camiones Disponibles** - Ver camiones libres
7. **Asignar Camión** - Asignar camión al primer tramo
8. **Iniciar Tramo** - Iniciar el traslado
9. **Seguimiento** - Consultar estado
10. **Finalizar Tramo** - Completar el traslado

---

## Exportar/Importar Colección

Para compartir la colección con tu equipo:

1. En Postman, click derecho en la colección
2. "Export"
3. Selecciona "Collection v2.1"
4. Guarda el archivo JSON
5. Comparte el archivo con tu equipo
6. Ellos pueden importarlo con "Import" en Postman

---

¡Felices pruebas! 🧪
