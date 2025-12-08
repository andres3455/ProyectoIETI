#!/bin/bash
echo "=== VALIDANDO SERVICIO ==="

# 1. Verificar proceso Spring Boot
if pgrep -f 'java.*application.jar' > /dev/null; then
    echo "✅ Proceso Spring Boot está corriendo"
else
    echo "❌ Proceso Spring Boot NO está corriendo"
    exit 1
fi

# 2. Verificar Spring Boot en puerto 8080
echo "Probando Spring Boot (puerto 8080)..."
if curl -s --max-time 10 http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ Spring Boot responde en puerto 8080"
else
    echo "❌ Spring Boot NO responde en puerto 8080"
    exit 1
fi

# 3. Verificar Nginx en puerto 80
echo "Probando Nginx proxy (puerto 80)..."
if curl -s --max-time 10 http://localhost > /dev/null 2>&1; then
    echo "✅ Nginx proxy funcionando en puerto 80"
else
    echo "⚠️ Nginx puede tener problemas"
    # Verificar estado de Nginx
    sudo systemctl status nginx --no-pager
fi

# 4. Verificar endpoints específicos
echo "Probando endpoints de aplicación..."
ENDPOINTS=("/swagger-ui.html" "/api-docs")
for endpoint in "${ENDPOINTS[@]}"; do
    if curl -s --max-time 5 "http://localhost:8080$endpoint" > /dev/null 2>&1; then
        echo "✅ Endpoint $endpoint accesible"
    fi
done

echo "✅ Validación completada exitosamente"
exit 0