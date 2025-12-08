#!/bin/bash
echo "=== INSTALANDO DEPENDENCIAS EN AMAZON LINUX ==="
set -e

# 1. Actualizar sistema
sudo yum update -y

# 2. Instalar Java 21
echo "Instalando Java 21..."
if sudo yum install -y java-21-amazon-corretto 2>/dev/null; then
    echo "✅ Java 21 instalado via java-21-amazon-corretto"
elif sudo yum install -y java-21 2>/dev/null; then
    echo "✅ Java 21 instalado via java-21"
else
    echo "Instalando OpenJDK 21..."
    sudo amazon-linux-extras enable corretto8
    sudo yum install -y java-1.8.0-amazon-corretto
fi

# 3. Verificar instalación
echo "=== VERIFICANDO JAVA ==="
if ! command -v java &> /dev/null; then
    echo "❌ ERROR: Java no se instaló"
    exit 1
fi

java -version

# 4. Instalar curl si no existe
if ! command -v curl &> /dev/null; then
    echo "Instalando curl..."
    sudo yum install -y curl
fi

# 5. Instalar Nginx (para proxy reverso)
echo "Instalando Nginx..."
sudo yum install -y nginx

# 6. Crear directorios necesarios
sudo mkdir -p /home/ec2-user/app /var/log/myapp
sudo chown -R ec2-user:ec2-user /home/ec2-user/app /var/log/myapp

echo "✅ Dependencias instaladas correctamente"