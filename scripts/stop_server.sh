#!/bin/bash
echo "=== DETENIENDO APLICACIÓN ==="

# Buscar y detener el proceso Java de la aplicación
APP_PID=$(pgrep -f 'java.*application.jar')

if [ ! -z "$APP_PID" ]; then
    echo "Deteniendo aplicación con PID: $APP_PID"
    kill $APP_PID

    # Esperar máximo 20 segundos
    for i in {1..20}; do
        if pgrep -f 'java.*application.jar' > /dev/null; then
            sleep 1
        else
            break
        fi
    done

    # Forzar kill si aún está corriendo
    if pgrep -f 'java.*application.jar' > /dev/null; then
        echo "Forzando terminación..."
        kill -9 $APP_PID
    fi
fi

echo "✅ Aplicación detenida correctamente"