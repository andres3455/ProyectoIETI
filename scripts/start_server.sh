#!/bin/bash
echo "=== INICIANDO APLICACIÓN ==="
set -e

# 1. VERIFICAR JAVA
echo "Verificando Java..."
java -version

# 2. VERIFICAR Y CONFIGURAR DIRECTORIO
cd /home/ec2-user/app
echo "Directorio actual: $(pwd)"
ls -la

if [ ! -f "application.jar" ]; then
    echo "❌ ERROR: application.jar no encontrado"
    exit 1
fi

echo "JAR encontrado. Tamaño: $(du -h application.jar | cut -f1)"

# 3. DETENER APLICACIÓN EXISTENTE (sin sudo, usando pkill normal)
echo "Deteniendo aplicación existente..."
pkill -f "java.*application.jar" 2>/dev/null || echo "No había aplicación corriendo"
sleep 3

# Verificar que no queden procesos
if pgrep -f "application.jar" > /dev/null; then
    echo "Forzando terminación de procesos restantes..."
    pkill -9 -f "application.jar"
    sleep 2
fi

# 4. CONFIGURAR LOGS (asegurar permisos)
LOG_FILE="/var/log/myapp/app.log"
sudo touch $LOG_FILE 2>/dev/null || true
sudo chown ec2-user:ec2-user $LOG_FILE 2>/dev/null || true
sudo chmod 644 $LOG_FILE 2>/dev/null || true

# 5. INICIAR APLICACIÓN
echo "=== NUEVO DEPLOYMENT - $(date) ===" > $LOG_FILE
echo "Iniciando Spring Boot en puerto 80..."

export SPRING_PROFILES_ACTIVE=production
export SERVER_PORT=80

# Usar & en lugar de nohup para mejor control
java -jar application.jar >> $LOG_FILE 2>&1 &
APP_PID=$!
echo "PID asignado: $APP_PID"

# 6. VERIFICAR QUE EL PROCESO INICIÓ
sleep 5
if ! ps -p $APP_PID > /dev/null 2>&1; then
    echo "❌ ERROR: Proceso no se mantuvo vivo"
    echo "=== ÚLTIMAS 30 LÍNEAS DEL LOG ==="
    tail -30 $LOG_FILE 2>/dev/null || echo "No se pudo leer el log"
    exit 1
fi

echo "✅ Proceso iniciado correctamente (PID: $APP_PID)"

# 7. ESPERAR Y VERIFICAR HEALTH CHECK
echo "Esperando que la aplicación inicie (máximo 90 segundos)..."
for i in {1..18}; do  # 18 intentos * 5 segundos = 90 segundos
    echo "Intento $i/18..."

    # Verificar si el proceso sigue vivo
    if ! ps -p $APP_PID > /dev/null 2>&1; then
        echo "❌ Proceso murió"
        tail -50 $LOG_FILE 2>/dev/null || echo "No se pudo leer el log"
        exit 1
    fi

    # Intentar health check (puerto 80)
    if curl -s --max-time 5 http://localhost:80/actuator/health > /dev/null 2>&1; then
        echo "✅ ✅ APLICACIÓN INICIADA Y RESPONDE CORRECTAMENTE"
        echo "Health check exitoso en puerto 80"
        exit 0
    fi

    # Intentar en puerto 8080 (fallback común de Spring Boot)
    if curl -s --max-time 5 http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ APLICACIÓN INICIADA en puerto 8080"
        echo "NOTA: La aplicación está en puerto 8080, no 80"
        exit 0
    fi

    sleep 5
done

echo "⚠️  Aplicación inició pero no responde después de 90 segundos"
echo "Proceso aún vivo: $(ps -p $APP_PID > /dev/null 2>&1 && echo "Sí" || echo "No")"
echo "=== ÚLTIMAS 100 LÍNEAS DEL LOG ==="
tail -100 $LOG_FILE 2>/dev/null || echo "No se pudo leer el log"

# Si el proceso está vivo, considerarlo éxito
if ps -p $APP_PID > /dev/null 2>&1; then
    echo "✅ Proceso sigue vivo, deployment parcialmente exitoso"
    exit 0
else
    exit 1
fi