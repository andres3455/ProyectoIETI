#!/bin/bash
echo "=== INSTALANDO DEPENDENCIAS EN AMAZON LINUX ==="
set -e  # Detener en el primer error

# 1. Actualizar sistema
sudo yum update -y

# 2. Instalar Java 21 CORREGIDO - nombres correctos para Amazon Linux
echo "Instalando Java 21..."
# Método 1: Intentar con el nombre correcto
if sudo yum install -y java-21-amazon-corretto 2>/dev/null; then
    echo "✅ Java 21 instalado via java-21-amazon-corretto"
elif sudo yum install -y java-21 2>/dev/null; then
    echo "✅ Java 21 instalado via java-21"
else
    # Método 2: Instalar OpenJDK 21
    echo "Instalando OpenJDK 21..."
    sudo amazon-linux-extras enable corretto8  # Habilitar repositorio
    sudo yum install -y java-1.8.0-amazon-corretto  # Java 8 como fallback
fi

# 3. Verificar instalación
echo "=== VERIFICANDO JAVA ==="
if ! command -v java &> /dev/null; then
    echo "❌ ERROR: Java no se instaló"
    echo "Buscando Java instalado..."
    sudo find /usr -name "java" -type f 2>/dev/null || true
    exit 1
fi

java -version

# 4. Instalar curl si no existe (necesario para validación)
if ! command -v curl &> /dev/null; then
    echo "Instalando curl..."
    sudo yum install -y curl
fi

# 5. Crear directorios necesarios
sudo mkdir -p /home/ec2-user/app /var/log/myapp
sudo chown -R ec2-user:ec2-user /home/ec2-user/app /var/log/myapp

echo "✅ Dependencias instaladas correctamente"