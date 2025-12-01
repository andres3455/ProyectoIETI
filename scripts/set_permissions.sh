#!/bin/bash
echo "=== CONFIGURANDO PERMISOS ==="

# Dar permisos al JAR
sudo chown ubuntu:ubuntu /home/ubuntu/app/application.jar
sudo chmod 755 /home/ubuntu/app/application.jar

# Crear log directory
sudo mkdir -p /var/log/myapp
sudo chown ubuntu:ubuntu /var/log/myapp

echo "✅ Permisos configurados correctamente"