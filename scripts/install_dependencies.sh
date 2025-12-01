#!/bin/bash
echo "=== INSTALANDO DEPENDENCIAS DEL SISTEMA ==="

# Actualizar sistema
sudo apt-get update -y

# Instalar Java 21
sudo apt-get install -y fontconfig openjdk-21-jre

# Verificar instalación
java -version
echo "✅ Java instalado correctamente"

# Crear directorio de la aplicación
sudo mkdir -p /home/ubuntu/app
sudo chown ubuntu:ubuntu /home/ubuntu/app

echo "✅ Dependencias instaladas correctamente"