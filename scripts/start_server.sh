#!/bin/bash
echo "=== INICIANDO APLICACIÓN ==="

# Detener instancia previa si existe
pkill -f 'java.*application.jar' || true

# Esperar un poco
sleep 5

# Navegar al directorio de la app
cd /home/ubuntu/app

# Exportar variables de entorno si es necesario
export SPRING_PROFILES_ACTIVE=production
export SERVER_PORT=80

# Iniciar la aplicación en background y redirigir logs
nohup java -jar application.jar > /var/log/myapp/app.log 2>&1 &

# Esperar a que la aplicación inicie
sleep 15

# Verificar que esté corriendo
if pgrep -f 'java.*application.jar' > /dev/null; then
    echo "✅ Aplicación iniciada correctamente"
    echo "PID: $(pgrep -f 'java.*application.jar')"
else
    echo "❌ Error al iniciar la aplicación"
    exit 1
fi