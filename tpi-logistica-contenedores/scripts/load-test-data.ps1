# Script para cargar datos de prueba en el sistema
Write-Host "=== Cargando datos de prueba ===" -ForegroundColor Cyan

$API_URL = "http://localhost:8080"

# Obtener token
Write-Host "1. Obteniendo token..." -ForegroundColor Yellow
$tokenBody = "username=operador1&password=operador123&grant_type=password&client_id=logistica-api"
$tokenResponse = Invoke-RestMethod -Uri "http://localhost:8180/realms/logistica-realm/protocol/openid-connect/token" -Method Post -Body $tokenBody -ContentType "application/x-www-form-urlencoded"
$token = $tokenResponse.access_token
Write-Host "   OK Token obtenido" -ForegroundColor Green

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# Crear clientes
Write-Host "2. Creando clientes..." -ForegroundColor Yellow
$clientes = @(
    @{
        nombre = "Juan"
        apellido = "Perez"
        email = "juan.perez@test.com"
        telefono = "1134567890"
        domicilio = "Av. Corrientes 1234, CABA"
        cuit = "20-30123456-7"
    },
    @{
        nombre = "Maria"
        apellido = "Gomez"
        email = "maria.gomez@test.com"
        telefono = "1145678901"
        domicilio = "Av. Santa Fe 5678, CABA"
        cuit = "27-35234567-8"
    },
    @{
        nombre = "Carlos"
        apellido = "Rodriguez"
        email = "carlos.rodriguez@test.com"
        telefono = "1156789012"
        domicilio = "Av. Rivadavia 9012, CABA"
        cuit = "20-40345678-9"
    }
)

foreach ($cliente in $clientes) {
    $clienteJson = $cliente | ConvertTo-Json
    try {
        $result = Invoke-RestMethod -Uri "$API_URL/api/clientes" -Method Post -Headers $headers -Body $clienteJson
        Write-Host "   OK Cliente $($cliente.nombre) $($cliente.apellido) creado (ID: $($result.id))" -ForegroundColor Green
    } catch {
        Write-Host "   ERROR creando cliente $($cliente.nombre): $_" -ForegroundColor Red
    }
}

# Crear depositos
Write-Host "3. Creando depositos..." -ForegroundColor Yellow
$depositos = @(
    @{
        nombre = "Deposito Norte"
        direccion = "Ruta 9 Km 45, Escobar"
        latitud = -34.3500
        longitud = -58.8000
        capacidadMaxima = 500
    },
    @{
        nombre = "Deposito Sur"
        direccion = "Av. Calchaqui 3000, Quilmes"
        latitud = -34.7200
        longitud = -58.2700
        capacidadMaxima = 300
    }
)

foreach ($deposito in $depositos) {
    $depositoJson = $deposito | ConvertTo-Json
    try {
        $result = Invoke-RestMethod -Uri "$API_URL/api/depositos" -Method Post -Headers $headers -Body $depositoJson
        Write-Host "   OK Deposito $($deposito.nombre) creado (ID: $($result.id))" -ForegroundColor Green
    } catch {
        Write-Host "   ERROR creando deposito $($deposito.nombre): $_" -ForegroundColor Red
    }
}

# Crear camiones
Write-Host "4. Creando camiones..." -ForegroundColor Yellow
$camiones = @(
    @{
        patente = "AA123BB"
        marca = "Mercedes Benz"
        modelo = "Actros"
        capacidadContenedores = 2
        estado = "DISPONIBLE"
    },
    @{
        patente = "CC456DD"
        marca = "Scania"
        modelo = "R450"
        capacidadContenedores = 2
        estado = "DISPONIBLE"
    },
    @{
        patente = "EE789FF"
        marca = "Volvo"
        modelo = "FH16"
        capacidadContenedores = 1
        estado = "DISPONIBLE"
    }
)

foreach ($camion in $camiones) {
    $camionJson = $camion | ConvertTo-Json
    try {
        $result = Invoke-RestMethod -Uri "$API_URL/api/camiones" -Method Post -Headers $headers -Body $camionJson
        Write-Host "   OK Camion $($camion.patente) creado (ID: $($result.id))" -ForegroundColor Green
    } catch {
        Write-Host "   ERROR creando camion $($camion.patente): $_" -ForegroundColor Red
    }
}

# Crear tarifas
Write-Host "5. Creando tarifas..." -ForegroundColor Yellow
$tarifas = @(
    @{
        tipoContenedor = "ESTANDAR_20"
        precioBase = 15000.00
        precioKilometro = 50.00
    },
    @{
        tipoContenedor = "ESTANDAR_40"
        precioBase = 25000.00
        precioKilometro = 80.00
    },
    @{
        tipoContenedor = "REFRIGERADO_20"
        precioBase = 20000.00
        precioKilometro = 70.00
    },
    @{
        tipoContenedor = "REFRIGERADO_40"
        precioBase = 35000.00
        precioKilometro = 110.00
    }
)

foreach ($tarifa in $tarifas) {
    $tarifaJson = $tarifa | ConvertTo-Json
    try {
        $result = Invoke-RestMethod -Uri "$API_URL/api/tarifas" -Method Post -Headers $headers -Body $tarifaJson
        Write-Host "   OK Tarifa $($tarifa.tipoContenedor) creada (ID: $($result.id))" -ForegroundColor Green
    } catch {
        Write-Host "   ERROR creando tarifa $($tarifa.tipoContenedor): $_" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== Datos de prueba cargados ===" -ForegroundColor Green
Write-Host ""
Write-Host "Puedes verificar los datos en:" -ForegroundColor Cyan
Write-Host "  GET http://localhost:8080/api/clientes" -ForegroundColor White
Write-Host "  GET http://localhost:8080/api/depositos" -ForegroundColor White
Write-Host "  GET http://localhost:8080/api/camiones" -ForegroundColor White
Write-Host "  GET http://localhost:8080/api/tarifas" -ForegroundColor White
