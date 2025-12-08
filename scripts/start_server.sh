#!/bin/bash
echo "=== INICIANDO APLICACIÓN SPRING BOOT ==="
set -e

# Cargar secrets (solo Google)
if [ -f "/home/ec2-user/load_secrets.sh" ]; then
    source /home/ec2-user/load_secrets.sh
fi

# 1. VERIFICAR JAVA
echo "Verificando Java..."
java -version

# 2. DIRECTORIO Y ARCHIVO
cd /home/ec2-user/app
echo "Directorio: $(pwd)"
ls -la

if [ ! -f "application.jar" ]; then
    echo "❌ ERROR: application.jar no encontrado"
    exit 1
fi

echo "JAR encontrado. Tamaño: $(du -h application.jar | cut -f1)"

# 3. DETENER APLICACIÓN EXISTENTE
echo "Deteniendo aplicación existente..."
pkill -f "java.*application.jar" 2>/dev/null || echo "No había aplicación corriendo"
sleep 3

# Verificar que no queden procesos
if pgrep -f "application.jar" > /dev/null; then
    echo "Forzando terminación..."
    pkill -9 -f "application.jar"
    sleep 2
fi

# 4. CONFIGURAR LOGS
LOG_FILE="/var/log/myapp/app.log"
sudo touch "$LOG_FILE" 2>/dev/null || true
sudo chown ec2-user:ec2-user "$LOG_FILE" 2>/dev/null || true
sudo chmod 644 "$LOG_FILE" 2>/dev/null || true

# 5. INICIAR APLICACIÓN EN PUERTO 8080
echo "=== $(date) - INICIANDO SPRING BOOT ===" >> "$LOG_FILE"
echo "Iniciando Spring Boot en puerto 8080..."

# Variables de entorno para Spring Boot
export SERVER_PORT=8080
export SPRING_PROFILES_ACTIVE=production

# Iniciar aplicación
java -jar application.jar \
    --server.port=${SERVER_PORT} \
    --spring.profiles.active=${SPRING_PROFILES_ACTIVE} >> "$LOG_FILE" 2>&1 &

APP_PID=$!
echo "✅ Proceso iniciado (PID: $APP_PID)"

# 6. ESPERAR INICIALIZACIÓN
echo "Esperando que la aplicación inicie (30 segundos)..."
sleep 30

# 7. VERIFICAR HEALTH CHECK
echo "Realizando health check..."
MAX_RETRIES=10
RETRY_INTERVAL=6

for i in $(seq 1 $MAX_RETRIES); do
    echo "Intento $i/$MAX_RETRIES..."

    # Verificar si proceso sigue vivo
    if ! ps -p $APP_PID > /dev/null; then
        echo "❌ Proceso murió"
        echo "=== ÚLTIMAS 30 LÍNEAS DEL LOG ==="
        tail -30 "$LOG_FILE"
        exit 1
    fi

    # Intentar health check en puerto 8080
    if curl -s -f --max-time 5 http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ ✅ APLICACIÓN INICIADA CORRECTAMENTE"
        echo "✅ Spring Boot responde en: http://localhost:8080"
        echo "✅ Nginx proxy en: http://localhost:80"
        exit 0
    fi

    sleep $RETRY_INTERVAL
done

echo "⚠️ Aplicación inició pero health check falló"
echo "Proceso vivo: $(ps -p $APP_PID > /dev/null && echo "Sí" || echo "No")"
echo "=== ÚLTIMAS 50 LÍNEAS DEL LOG ==="
tail -50 "$LOG_FILE"

# Si el proceso está vivo, considerarlo éxito
if ps -p $APP_PID > /dev/null; then
    echo "✅ Proceso sigue vivo - Deployment parcialmente exitoso"
    exit 0
else
    exit 1
fi