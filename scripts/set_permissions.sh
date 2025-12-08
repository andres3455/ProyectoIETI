#!/bin/bash
echo "=== CONFIGURANDO PERMISOS ==="
set -e

# 1. Configurar directorio de la aplicación
sudo mkdir -p /home/ec2-user/app
sudo chown -R ec2-user:ec2-user /home/ec2-user/app
sudo chmod -R 755 /home/ec2-user/app

# 2. Configurar directorio de logs con permisos correctos
sudo mkdir -p /var/log/myapp
sudo chown -R ec2-user:ec2-user /var/log/myapp
sudo chmod -R 755 /var/log/myapp

# 3. Crear archivo de log con permisos
sudo touch /var/log/myapp/app.log
sudo chown ec2-user:ec2-user /var/log/myapp/app.log
sudo chmod 644 /var/log/myapp/app.log

echo "✅ Permisos configurados"