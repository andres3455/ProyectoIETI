#!/bin/bash
echo "=== CONFIGURANDO PERMISOS ==="

# Dar permisos completos
sudo chown -R ec2-user:ec2-user /home/ec2-user/app
sudo chmod -R 755 /home/ec2-user/app
sudo chmod +x /home/ec2-user/app/application.jar

# Crear directorio de logs
sudo mkdir -p /var/log/myapp
sudo chown -R ec2-user:ec2-user /var/log/myapp
sudo chmod -R 755 /var/log/myapp

echo "✅ Permisos configurados"