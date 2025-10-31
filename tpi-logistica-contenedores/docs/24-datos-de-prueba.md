# Datos de Prueba - Sistema de Logística de Contenedores

## Descripción General

Este documento describe los datos de prueba precargados en el sistema para facilitar el testing y demostración de funcionalidades.

## Ejecución de Scripts

Los scripts de datos de prueba se ejecutan automáticamente al iniciar cada microservicio con el perfil de desarrollo.

### Configuración Automática

En `application.yml` de cada servicio:

\`\`\`yaml
spring:
  jpa:
    defer-datasource-initialization: true
  sql:
    init:
      mode: always
      data-locations: classpath:data-test.sql
\`\`\`

### Ejecución Manual

Si necesitas ejecutar los scripts manualmente:

\`\`\`bash
# Desde el directorio de cada microservicio
docker exec -i postgres-db psql -U logistica -d db_clientes < src/main/resources/data-test.sql
docker exec -i postgres-db psql -U logistica -d db_depositos < src/main/resources/data-test.sql
# ... etc
\`\`\`

## Datos Precargados

### 1. Clientes (clientes-service)

| ID | Nombre | Email | Teléfono | Estado |
|----|--------|-------|----------|--------|
| 1 | Juan Pérez | juan.perez@email.com | +54 11 1234-5678 | Activo |
| 2 | María González | maria.gonzalez@email.com | +54 11 2345-6789 | Activo |
| 3 | Carlos Rodríguez | carlos.rodriguez@email.com | +54 11 3456-7890 | Activo |
| 4 | Ana Martínez | ana.martinez@email.com | +54 11 4567-8901 | Activo |
| 5 | Luis Fernández | luis.fernandez@email.com | +54 11 5678-9012 | Activo |
| 6 | Laura López | laura.lopez@email.com | +54 11 6789-0123 | Inactivo |
| 7 | Pedro Sánchez | pedro.sanchez@email.com | +54 11 7890-1234 | Activo |
| 8 | Sofía Ramírez | sofia.ramirez@email.com | +54 11 8901-2345 | Activo |

### 2. Depósitos (depositos-service)

| ID | Nombre | Ubicación | Capacidad | Ocupación | Costo/Día |
|----|--------|-----------|-----------|-----------|-----------|
| 1 | Depósito Central CABA | CABA | 100 | 15 | $5,000 |
| 2 | Depósito Norte GBA | San Fernando | 80 | 8 | $4,000 |
| 3 | Depósito Sur GBA | Avellaneda | 60 | 12 | $3,500 |
| 4 | Depósito Oeste GBA | La Matanza | 70 | 5 | $3,800 |
| 5 | Depósito Rosario | Rosario, Santa Fe | 90 | 20 | $4,500 |
| 6 | Depósito Córdoba | Córdoba Capital | 85 | 10 | $4,200 |
| 7 | Depósito Mendoza | Mendoza | 50 | 3 | $3,000 |
| 8 | Depósito La Plata | La Plata | 65 | 7 | $3,600 |

### 3. Transportistas y Camiones (camiones-service)

**Transportistas:**

| ID | Nombre | Licencia | Vencimiento | Estado |
|----|--------|----------|-------------|--------|
| 1 | Roberto Gómez | B2-12345678 | 2026-12-31 | Activo |
| 2 | Diego Torres | B2-23456789 | 2026-06-30 | Activo |
| 3 | Marcelo Silva | B2-34567890 | 2025-12-31 | Activo |
| 4 | Fernando Ruiz | B2-45678901 | 2027-03-31 | Activo |
| 5 | Gustavo Morales | B2-56789012 | 2026-09-30 | Activo |
| 6 | Javier Castro | B2-67890123 | 2025-06-30 | Inactivo |

**Camiones:**

| ID | Dominio | Marca/Modelo | Capacidad Peso | Capacidad Vol. | Disponible | Transportista |
|----|---------|--------------|----------------|----------------|------------|---------------|
| 1 | AB123CD | Mercedes-Benz Actros | 25,000 kg | 80 m³ | Sí | Roberto Gómez |
| 2 | EF456GH | Scania R450 | 30,000 kg | 90 m³ | Sí | Diego Torres |
| 3 | IJ789KL | Volvo FH16 | 28,000 kg | 85 m³ | No | Marcelo Silva |
| 4 | MN012OP | Iveco Stralis | 20,000 kg | 70 m³ | Sí | Fernando Ruiz |
| 5 | QR345ST | Mercedes-Benz Axor | 22,000 kg | 75 m³ | Sí | Gustavo Morales |
| 6 | UV678WX | Scania G410 | 26,000 kg | 82 m³ | No | Roberto Gómez |
| 7 | YZ901AB | Volvo FM380 | 24,000 kg | 78 m³ | Sí | Diego Torres |
| 8 | CD234EF | Iveco Trakker | 32,000 kg | 95 m³ | Sí | Marcelo Silva |

### 4. Tarifas (tarifas-service)

| Concepto | Descripción | Valor | Vigencia |
|----------|-------------|-------|----------|
| COSTO_KM_BASE_PEQUEÑO | Contenedores 0-10 ton, 0-30 m³ | $80/km | 2025 |
| COSTO_KM_BASE_MEDIANO | Contenedores 10-20 ton, 31-60 m³ | $120/km | 2025 |
| COSTO_KM_BASE_GRANDE | Contenedores 20-35 ton, 61-100 m³ | $180/km | 2025 |
| COSTO_COMBUSTIBLE_LITRO | Precio combustible | $950/litro | 2025 |
| CARGO_GESTION_1_TRAMO | Cargo fijo 1 tramo | $5,000 | 2025 |
| CARGO_GESTION_2_TRAMOS | Cargo fijo 2 tramos | $8,000 | 2025 |
| CARGO_GESTION_3_TRAMOS | Cargo fijo 3+ tramos | $12,000 | 2025 |

### 5. Solicitudes y Contenedores (solicitudes-service)

**Contenedores:**

| ID | Código | Peso | Volumen | Dimensiones | Estado |
|----|--------|------|---------|-------------|--------|
| 1 | CONT-2025-001 | 8,500 kg | 25 m³ | 6.0 × 2.4 × 2.6 m | EN_TRANSITO |
| 2 | CONT-2025-002 | 15,000 kg | 45 m³ | 12.0 × 2.4 × 2.6 m | EN_DEPOSITO |
| 3 | CONT-2025-003 | 12,000 kg | 35 m³ | 9.0 × 2.4 × 2.6 m | ENTREGADO |
| 4 | CONT-2025-004 | 9,500 kg | 28 m³ | 6.0 × 2.4 × 2.9 m | PENDIENTE_RETIRO |
| 5 | CONT-2025-005 | 18,000 kg | 50 m³ | 12.0 × 2.4 × 2.9 m | EN_TRANSITO |

**Solicitudes:**

| ID | Número | Cliente | Contenedor | Origen → Destino | Estado | Costo Est. |
|----|--------|---------|------------|------------------|--------|------------|
| 1 | SOL-2025-001 | Juan Pérez | CONT-2025-001 | CABA → San Fernando | EN_TRANSITO | $45,000 |
| 2 | SOL-2025-002 | María González | CONT-2025-002 | CABA → Córdoba | PROGRAMADA | $125,000 |
| 3 | SOL-2025-003 | Carlos Rodríguez | CONT-2025-003 | CABA → Rosario | ENTREGADA | $95,000 |
| 4 | SOL-2025-004 | Ana Martínez | CONT-2025-004 | CABA → Mendoza | BORRADOR | $180,000 |
| 5 | SOL-2025-005 | Luis Fernández | CONT-2025-005 | CABA → La Plata | EN_TRANSITO | $55,000 |

### 6. Rutas y Tramos (rutas-service)

**Rutas:**

| ID | Solicitud | Tramos | Depósitos | Distancia | Estado |
|----|-----------|--------|-----------|-----------|--------|
| 1 | SOL-2025-001 | 2 | 1 | 85.5 km | EN_PROGRESO |
| 2 | SOL-2025-002 | 3 | 2 | 720 km | ESTIMADA |
| 3 | SOL-2025-003 | 2 | 1 | 310 km | COMPLETADA |
| 5 | SOL-2025-005 | 1 | 0 | 65 km | EN_PROGRESO |

**Tramos destacados:**

- **Ruta 1**: CABA → Depósito Norte (completado) → San Fernando (en progreso)
- **Ruta 2**: CABA → Depósito Central → Depósito Rosario → Córdoba (estimada)
- **Ruta 3**: CABA → Depósito Sur → Rosario (completada con éxito)
- **Ruta 5**: CABA → La Plata (directo, sin depósitos)

## Escenarios de Prueba

### Escenario 1: Solicitud en Tránsito
- **Solicitud**: SOL-2025-001
- **Estado**: Primer tramo completado, segundo tramo en progreso
- **Prueba**: Seguimiento de contenedor, actualización de estado de tramo

### Escenario 2: Solicitud Programada
- **Solicitud**: SOL-2025-002
- **Estado**: Programada para inicio futuro
- **Prueba**: Asignación de camiones, cálculo de rutas

### Escenario 3: Solicitud Completada
- **Solicitud**: SOL-2025-003
- **Estado**: Entregada con costos reales calculados
- **Prueba**: Consulta de historial, cálculo de costos finales

### Escenario 4: Solicitud en Borrador
- **Solicitud**: SOL-2025-004
- **Estado**: Borrador sin programar
- **Prueba**: Edición de solicitud, cálculo de costos estimados

### Escenario 5: Envío Directo
- **Solicitud**: SOL-2025-005
- **Estado**: En tránsito sin depósitos intermedios
- **Prueba**: Ruta directa, menor complejidad

## Usuarios de Keycloak para Pruebas

Una vez configurado Keycloak, crear estos usuarios:

### Cliente
- **Username**: `cliente1`
- **Password**: `cliente123`
- **Rol**: `CLIENTE`
- **Permisos**: Crear solicitudes, consultar estado

### Operador
- **Username**: `operador1`
- **Password**: `operador123`
- **Rol**: `OPERADOR`
- **Permisos**: Gestionar todo el sistema

### Transportista
- **Username**: `transportista1`
- **Password**: `transportista123`
- **Rol**: `TRANSPORTISTA`
- **Permisos**: Ver tramos asignados, registrar inicio/fin

## Comandos Útiles para Testing

### Verificar datos cargados

\`\`\`bash
# Contar clientes
curl http://localhost:8080/api/clientes | jq 'length'

# Listar depósitos
curl http://localhost:8080/api/depositos | jq '.[].nombre'

# Ver camiones disponibles
curl http://localhost:8080/api/camiones/disponibles

# Ver solicitudes en tránsito
curl http://localhost:8080/api/solicitudes?estado=EN_TRANSITO

# Ver rutas en progreso
curl http://localhost:8080/api/rutas?estado=EN_PROGRESO
\`\`\`

### Limpiar datos de prueba

\`\`\`bash
# Detener contenedores y eliminar volúmenes
docker-compose down -v

# Reiniciar con datos frescos
docker-compose up -d
\`\`\`

## Notas Importantes

1. **IDs Secuenciales**: Los IDs comienzan desde 1 en cada tabla
2. **Fechas Relativas**: Las fechas usan `CURRENT_TIMESTAMP` con intervalos para simular datos históricos y futuros
3. **Consistencia**: Los datos están relacionados correctamente entre microservicios
4. **Realismo**: Los datos simulan escenarios reales de logística en Argentina

## Próximos Pasos

1. Ejecutar `docker-compose up -d` para iniciar el sistema
2. Verificar que los datos se cargaron correctamente
3. Probar los endpoints con los datos precargados
4. Configurar usuarios en Keycloak
5. Ejecutar la colección de pruebas de Postman/Bruno

---

**Última actualización**: Enero 2025
