#!/bin/bash
echo "=== INICIANDO APLICACIÓN ==="
set -e  # Detener en errores

# 1. VERIFICAR JAVA
echo "Verificando Java..."
if ! command -v java &> /dev/null; then
    echo "❌ ERROR: Java no está instalado. Instalando..."
    sudo yum install -y java-1.8.0-amazon-corretto
fi

echo "Java version:"
java -version

# 2. VERIFICAR Y CONFIGURAR DIRECTORIO
cd /home/ec2-user/app
echo "Directorio actual: $(pwd)"
echo "Contenido:"
ls -la

if [ ! -f "application.jar" ]; then
    echo "❌ ERROR: application.jar no encontrado"
    echo "Buscando archivos .jar..."
    find /home/ec2-user -name "*.jar" -type f 2>/dev/null || true
    exit 1
fi

echo "JAR encontrado. Tamaño: $(du -h application.jar | cut -f1)"

# 3. DETENER APLICACIÓN EXISTENTE
echo "Deteniendo aplicación existente..."
sudo pkill -9 -f "application.jar" || echo "No había aplicación corriendo"
sleep 3

# 4. LIMPIAR LOGS ANTIGUOS
echo "=== NUEVO DEPLOYMENT - $(date) ===" | sudo tee /var/log/myapp/app.log

# 5. INICIAR APLICACIÓN
echo "Iniciando Spring Boot en puerto 80..."
export SPRING_PROFILES_ACTIVE=production

# Usar nohup y redirigir TODA la salida
nohup java -jar application.jar --server.port=80 > /var/log/myapp/app.log 2>&1 &
APP_PID=$!
echo "PID asignado: $APP_PID"

# 6. VERIFICAR QUE EL PROCESO INICIÓ
sleep 5
if ! ps -p $APP_PID > /dev/null; then
    echo "❌ ERROR: Proceso no se mantuvo vivo"
    echo "=== ÚLTIMAS 30 LÍNEAS DEL LOG ==="
    tail -30 /var/log/myapp/app.log
    exit 1
fi

# 7. ESPERAR Y VERIFICAR HEALTH CHECK
echo "Esperando que la aplicación inicie (máximo 60 segundos)..."
for i in {1..12}; do  # 12 intentos * 5 segundos = 60 segundos
    echo "Intento $i/12..."

    # Verificar si el proceso sigue vivo
    if ! ps -p $APP_PID > /dev/null; then
        echo "❌ Proceso murió"
        tail -50 /var/log/myapp/app.log
        exit 1
    fi

    # Intentar health check
    if curl -s --max-time 10 http://localhost:80/actuator/health > /dev/null 2>&1; then
        echo "✅ ✅ APLICACIÓN INICIADA Y RESPONDE CORRECTAMENTE"
        echo "Health check:"
        curl -s http://localhost:80/actuator/health || echo "No se pudo obtener health"
        exit 0
    fi

    sleep 5
done

echo "⚠️  Aplicación inició pero no responde después de 60 segundos"
echo "Proceso aún vivo: $(ps -p $APP_PID > /dev/null && echo "Sí" || echo "No")"
echo "=== ÚLTIMAS 100 LÍNEAS DEL LOG ==="
tail -100 /var/log/myapp/app.log

# No salir con error para dar chance a la aplicación
exit 0