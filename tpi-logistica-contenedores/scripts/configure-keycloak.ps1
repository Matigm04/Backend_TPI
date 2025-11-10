# Script para configurar Keycloak
Write-Host "=== Configurando Keycloak ===" -ForegroundColor Cyan

$KEYCLOAK_URL = "http://localhost:8180"

# 1. Obtener token de admin
Write-Host "1. Obteniendo token de administrador..." -ForegroundColor Yellow
$adminBody = "username=admin&password=admin123&grant_type=password&client_id=admin-cli"
$adminToken = (Invoke-RestMethod -Uri "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" -Method Post -Body $adminBody -ContentType "application/x-www-form-urlencoded").access_token
Write-Host "   OK Token obtenido" -ForegroundColor Green

# 2. Crear realm
Write-Host "2. Creando realm logistica-realm..." -ForegroundColor Yellow
$headers = @{ "Authorization" = "Bearer $adminToken"; "Content-Type" = "application/json" }
$realmJson = '{"realm":"logistica-realm","enabled":true}'
try {
    Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms" -Method Post -Headers $headers -Body $realmJson | Out-Null
    Write-Host "   OK Realm creado" -ForegroundColor Green
} catch {
    Write-Host "   WARN Realm ya existe" -ForegroundColor Yellow
}

# 3. Crear roles
Write-Host "3. Creando roles..." -ForegroundColor Yellow
$roles = @("CLIENTE", "OPERADOR", "ADMIN", "TRANSPORTISTA")
foreach ($role in $roles) {
    $roleJson = "{`"name`":`"$role`"}"
    try {
        Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/logistica-realm/roles" -Method Post -Headers $headers -Body $roleJson | Out-Null
        Write-Host "   OK Rol $role creado" -ForegroundColor Green
    } catch {
        Write-Host "   WARN Rol $role ya existe" -ForegroundColor Yellow
    }
}

# 4. Crear cliente
Write-Host "4. Creando cliente logistica-api..." -ForegroundColor Yellow
$clientJson = @"
{
    "clientId": "logistica-api",
    "enabled": true,
    "publicClient": true,
    "directAccessGrantsEnabled": true,
    "redirectUris": ["*"],
    "webOrigins": ["*"]
}
"@
try {
    Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/logistica-realm/clients" -Method Post -Headers $headers -Body $clientJson | Out-Null
    Write-Host "   OK Cliente creado" -ForegroundColor Green
} catch {
    Write-Host "   WARN Cliente ya existe" -ForegroundColor Yellow
}

# 5. Crear usuarios
Write-Host "5. Creando usuarios..." -ForegroundColor Yellow

function Create-User($username, $password, $firstName, $lastName, $email, $roleName) {
    $userJson = @"
{
    "username": "$username",
    "enabled": true,
    "emailVerified": true,
    "firstName": "$firstName",
    "lastName": "$lastName",
    "email": "$email",
    "credentials": [{"type": "password", "value": "$password", "temporary": false}]
}
"@
    try {
        Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/logistica-realm/users" -Method Post -Headers $headers -Body $userJson | Out-Null
        Write-Host "   OK Usuario $username creado" -ForegroundColor Green
        
        Start-Sleep -Seconds 1
        $users = Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/logistica-realm/users?username=$username" -Method Get -Headers $headers
        $userId = $users[0].id
        
        $roleInfo = Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/logistica-realm/roles/$roleName" -Method Get -Headers $headers
        $roleJson = "[{`"id`":`"$($roleInfo.id)`",`"name`":`"$($roleInfo.name)`"}]"
        
        Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/logistica-realm/users/$userId/role-mappings/realm" -Method Post -Headers $headers -Body $roleJson | Out-Null
        Write-Host "   OK Rol $roleName asignado a $username" -ForegroundColor Green
    } catch {
        Write-Host "   WARN Usuario $username ya existe" -ForegroundColor Yellow
    }
}

Create-User "cliente1" "cliente123" "Juan" "Perez" "cliente1@test.com" "CLIENTE"
Create-User "operador1" "operador123" "Thomi" "Salomon" "operador1@test.com" "OPERADOR"
Create-User "admin1" "admin123" "Admin" "Sistema" "admin1@test.com" "ADMIN"
Create-User "transportista1" "transportista123" "Carlos" "Garcia" "transportista1@test.com" "TRANSPORTISTA"

# 6. Obtener token de prueba
Write-Host ""
Write-Host "=== Obteniendo token para operador1 ===" -ForegroundColor Cyan
$tokenBody = "username=operador1&password=operador123&grant_type=password&client_id=logistica-api"
$tokenResponse = Invoke-RestMethod -Uri "$KEYCLOAK_URL/realms/logistica-realm/protocol/openid-connect/token" -Method Post -Body $tokenBody -ContentType "application/x-www-form-urlencoded"

Write-Host ""
Write-Host "ACCESS TOKEN:" -ForegroundColor Yellow
Write-Host $tokenResponse.access_token -ForegroundColor White
Write-Host ""
Write-Host "Expira en: $($tokenResponse.expires_in) segundos" -ForegroundColor Yellow
Write-Host ""
Write-Host "=== Configuracion completada ===" -ForegroundColor Green
Write-Host ""
Write-Host "Usuarios:" -ForegroundColor Cyan
Write-Host "  cliente1 / cliente123" -ForegroundColor White
Write-Host "  operador1 / operador123" -ForegroundColor White
Write-Host "  admin1 / admin123" -ForegroundColor White
Write-Host "  transportista1 / transportista123" -ForegroundColor White
