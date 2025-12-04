#!/bin/bash
echo "=== INICIANDO APLICACIÓN EN AMAZON LINUX ==="

# Configurar variables
export SPRING_PROFILES_ACTIVE=production
export SERVER_PORT=80

# Navegar al directorio
cd /home/ec2-user/app

# Verificar JAR
if [ ! -f "application.jar" ]; then
    echo "❌ ERROR: application.jar no encontrado"
    exit 1
fi

# Detener aplicación previa
pkill -f "java.*application.jar" || echo "No hay aplicación previa"
sleep 5

# Iniciar aplicación
echo "Iniciando aplicación Spring Boot..."
nohup java -jar application.jar > /var/log/myapp/app.log 2>&1 &

# Esperar y verificar
sleep 10

if pgrep -f "java.*application.jar" > /dev/null; then
    echo "✅ Aplicación iniciada - PID: $(pgrep -f 'java.*application.jar')"
    sleep 20

    if curl -s http://localhost:80/actuator/health > /dev/null; then
        echo "✅ Aplicación respondiendo correctamente"
    else
        echo "⚠️  Aplicación iniciada pero no responde aún"
    fi
else
    echo "❌ ERROR: No se pudo iniciar la aplicación"
    tail -20 /var/log/myapp/app.log
    exit 1
fi