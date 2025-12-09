# Script de Prueba para el Endpoint de Autenticación
# Guarda este archivo como: test_auth.sh (Linux/Mac) o test_auth.ps1 (Windows)

# =============================================================================
# WINDOWS POWERSHELL VERSION
# =============================================================================

# Configuración
$baseUrl = "http://localhost:8080"
$testToken = "REEMPLAZA_CON_UN_TOKEN_REAL_DE_GOOGLE"

Write-Host "🧪 Testing OAuth Authentication Endpoints" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Health Check
Write-Host "📍 Test 1: Health Check" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/health" -Method GET
    Write-Host "✅ Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "   Response: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 2: Auth Status (Sin autenticación)
Write-Host "📍 Test 2: Auth Status (No Token)" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/auth/status" -Method GET
    Write-Host "✅ Status: $($response.StatusCode)" -ForegroundColor Green
    $content = $response.Content | ConvertFrom-Json
    Write-Host "   Authenticated: $($content.authenticated)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 3: Verify Token (Requiere token real)
Write-Host "📍 Test 3: Verify Token" -ForegroundColor Yellow
if ($testToken -eq "REEMPLAZA_CON_UN_TOKEN_REAL_DE_GOOGLE") {
    Write-Host "⚠️  Skipped: Por favor proporciona un token real de Google" -ForegroundColor Yellow
    Write-Host "   Para obtener un token:" -ForegroundColor Gray
    Write-Host "   1. Implementa el código Flutter actualizado" -ForegroundColor Gray
    Write-Host "   2. Ejecuta la app y haz login con Google" -ForegroundColor Gray
    Write-Host "   3. Copia el token del log (busca 'ID Token')" -ForegroundColor Gray
    Write-Host "   4. Reemplaza el valor de `$testToken en este script" -ForegroundColor Gray
} else {
    try {
        $body = @{
            idToken = $testToken
        } | ConvertTo-Json

        $response = Invoke-WebRequest `
            -Uri "$baseUrl/api/auth/verify" `
            -Method POST `
            -ContentType "application/json" `
            -Body $body

        Write-Host "✅ Status: $($response.StatusCode)" -ForegroundColor Green
        $content = $response.Content | ConvertFrom-Json
        Write-Host "   Authenticated: $($content.authenticated)" -ForegroundColor Gray
        Write-Host "   User ID: $($content.user.id)" -ForegroundColor Gray
        Write-Host "   User Name: $($content.user.name)" -ForegroundColor Gray
        Write-Host "   User Email: $($content.user.email)" -ForegroundColor Gray
    } catch {
        Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "   Response: $responseBody" -ForegroundColor Red
        }
    }
}
Write-Host ""

# Test 4: Get User Profile (Con token)
Write-Host "📍 Test 4: Get User Profile (With Token)" -ForegroundColor Yellow
if ($testToken -eq "REEMPLAZA_CON_UN_TOKEN_REAL_DE_GOOGLE") {
    Write-Host "⚠️  Skipped: Requiere token real de Google" -ForegroundColor Yellow
} else {
    try {
        $headers = @{
            "Authorization" = "Bearer $testToken"
        }

        $response = Invoke-WebRequest `
            -Uri "$baseUrl/api/user/profile" `
            -Method GET `
            -Headers $headers

        Write-Host "✅ Status: $($response.StatusCode)" -ForegroundColor Green
        $content = $response.Content | ConvertFrom-Json
        Write-Host "   User ID: $($content.id)" -ForegroundColor Gray
        Write-Host "   User Name: $($content.name)" -ForegroundColor Gray
        Write-Host "   User Email: $($content.email)" -ForegroundColor Gray
    } catch {
        Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}
Write-Host ""

# Test 5: Get User Profile (Sin token - debería fallar)
Write-Host "📍 Test 5: Get User Profile (No Token)" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/user/profile" -Method GET
    Write-Host "⚠️  Status: $($response.StatusCode) - Debería haber fallado con 401" -ForegroundColor Yellow
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "✅ Correctamente retornó 401 Unauthorized" -ForegroundColor Green
    } else {
        Write-Host "❌ Error inesperado: $($_.Exception.Message)" -ForegroundColor Red
    }
}
Write-Host ""

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "🎉 Tests completados" -ForegroundColor Cyan
Write-Host ""
Write-Host "💡 Próximos pasos:" -ForegroundColor Cyan
Write-Host "   1. Configura Google OAuth credentials en application.properties" -ForegroundColor Gray
Write-Host "   2. Obtén un token real desde Flutter" -ForegroundColor Gray
Write-Host "   3. Reemplaza `$testToken y ejecuta este script nuevamente" -ForegroundColor Gray
Write-Host ""

# =============================================================================
# BASH VERSION (Para Linux/Mac)
# =============================================================================
<#
#!/bin/bash

# Configuración
BASE_URL="http://localhost:8080"
TEST_TOKEN="REEMPLAZA_CON_UN_TOKEN_REAL_DE_GOOGLE"

echo "🧪 Testing OAuth Authentication Endpoints"
echo "========================================="
echo ""

# Test 1: Health Check
echo "📍 Test 1: Health Check"
curl -s -X GET "$BASE_URL/health" -w "\n✅ Status: %{http_code}\n\n"

# Test 2: Auth Status (Sin autenticación)
echo "📍 Test 2: Auth Status (No Token)"
curl -s -X GET "$BASE_URL/api/auth/status" -w "\n✅ Status: %{http_code}\n\n"

# Test 3: Verify Token
echo "📍 Test 3: Verify Token"
if [ "$TEST_TOKEN" == "REEMPLAZA_CON_UN_TOKEN_REAL_DE_GOOGLE" ]; then
    echo "⚠️  Skipped: Por favor proporciona un token real de Google"
    echo ""
else
    curl -s -X POST "$BASE_URL/api/auth/verify" \
        -H "Content-Type: application/json" \
        -d "{\"idToken\": \"$TEST_TOKEN\"}" \
        -w "\n✅ Status: %{http_code}\n\n"
fi

# Test 4: Get User Profile (Con token)
echo "📍 Test 4: Get User Profile (With Token)"
if [ "$TEST_TOKEN" == "REEMPLAZA_CON_UN_TOKEN_REAL_DE_GOOGLE" ]; then
    echo "⚠️  Skipped: Requiere token real de Google"
    echo ""
else
    curl -s -X GET "$BASE_URL/api/user/profile" \
        -H "Authorization: Bearer $TEST_TOKEN" \
        -w "\n✅ Status: %{http_code}\n\n"
fi

# Test 5: Get User Profile (Sin token)
echo "📍 Test 5: Get User Profile (No Token)"
curl -s -X GET "$BASE_URL/api/user/profile" \
    -w "\n✅ Status: %{http_code} (debería ser 401)\n\n"

echo "========================================="
echo "🎉 Tests completados"
echo ""
#>
