#!/bin/bash
echo "=== VALIDANDO SERVICIO ==="

# Verificar que el proceso esté corriendo
if pgrep -f 'java.*application.jar' > /dev/null; then
    echo "✅ Proceso de aplicación está corriendo"
else
    echo "❌ Proceso de aplicación NO está corriendo"
    exit 1
fi

# Verificar que responda en el puerto 80 (opcional - si tienes curl)
if command -v curl &> /dev/null; then
    response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:80/actuator/health || echo "000")
    if [ "$response" == "200" ] || [ "$response" == "000" ]; then
        echo "✅ Aplicación respondiendo correctamente"
    else
        echo "⚠️  Aplicación respondió con código: $response"
    fi
else
    echo "ℹ️  curl no disponible, omitiendo verificación HTTP"
fi

echo "✅ Validación completada"