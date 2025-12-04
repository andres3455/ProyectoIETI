#!/bin/bash
echo "=== INSTALANDO DEPENDENCIAS EN AMAZON LINUX ==="

# Actualizar sistema
sudo yum update -y

# Instalar Java 21 en Amazon Linux
sudo yum install -y java-21-amazon-corretto-devel

# Verificar instalación
echo "Java version:"
java -version
echo "JAVA_HOME:"
echo $JAVA_HOME

# Configurar JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto
echo "export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto" >> ~/.bashrc

echo "✅ Dependencias instaladas en Amazon Linux"